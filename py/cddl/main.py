# Licensed to the Software Freedom Conservancy (SFC) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The SFC licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""CLI for CDDL parser and Python code generator."""

import argparse
import logging
from pathlib import Path
from typing import Optional

from . import downloader, parser, writer


def setup_logging(verbose: bool = False):
    """Set up logging."""
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    )


def main():
    """Main entry point for CLI."""
    parser_cli = argparse.ArgumentParser(
        description="CDDL parser and Python code generator for WebDriver BiDi"
    )

    subparsers = parser_cli.add_subparsers(dest="command", help="Command to run")

    # Download command
    download_parser = subparsers.add_parser(
        "download", help="Download W3C WebDriver BiDi CDDL specification"
    )
    download_parser.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="Directory to save CDDL files (default: py/cddl/specs/)",
    )
    download_parser.add_argument(
        "--force",
        action="store_true",
        help="Force re-download even if files exist",
    )

    # Parse command
    parse_parser = subparsers.add_parser("parse", help="Parse CDDL specification")
    parse_parser.add_argument(
        "cddl_path",
        type=Path,
        help="Path to CDDL file or directory",
    )
    parse_parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Output path for parsed AST (JSON format)",
    )

    # Generate command
    generate_parser = subparsers.add_parser(
        "generate", help="Generate Python code from CDDL"
    )
    generate_parser.add_argument(
        "cddl_path",
        type=Path,
        help="Path to CDDL file or directory",
    )
    generate_parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).parent.parent / "selenium" / "webdriver" / "common" / "bidi",
        help="Directory for generated Python files",
    )
    generate_parser.add_argument(
        "--strict",
        action="store_true",
        help="Enable strict validation mode",
    )

    # Full workflow command
    workflow_parser = subparsers.add_parser(
        "generate-all", help="Download, parse, and generate (full workflow)"
    )
    workflow_parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).parent.parent / "selenium" / "webdriver" / "common" / "bidi",
        help="Directory for generated Python files",
    )
    workflow_parser.add_argument(
        "--cddl-output-dir",
        type=Path,
        default=None,
        help="Directory for CDDL files (default: py/cddl/specs/)",
    )
    workflow_parser.add_argument(
        "--strict",
        action="store_true",
        help="Enable strict validation mode",
    )

    # Global options
    parser_cli.add_argument(
        "-v", "--verbose", action="store_true", help="Enable verbose logging"
    )

    args = parser_cli.parse_args()
    setup_logging(args.verbose)

    if not args.command:
        parser_cli.print_help()
        return 0

    try:
        if args.command == "download":
            return cmd_download(args)
        elif args.command == "parse":
            return cmd_parse(args)
        elif args.command == "generate":
            return cmd_generate(args)
        elif args.command == "generate-all":
            return cmd_generate_all(args)
        else:
            parser_cli.print_help()
            return 1
    except Exception as e:
        logging.error(f"Error: {e}", exc_info=args.verbose)
        return 1


def cmd_download(args) -> int:
    """Handle download command."""
    logging.info("Downloading CDDL specification...")
    output_dir = downloader.download_cddl_spec(
        output_dir=args.output_dir,
        force=args.force,
    )
    logging.info(f"CDDL files downloaded to {output_dir}")

    # List downloaded files
    cddl_files = downloader.find_cddl_files(output_dir)
    logging.info(f"Found {len(cddl_files)} CDDL files:")
    for file in cddl_files:
        logging.info(f"  - {file.name}")

    return 0


def cmd_parse(args) -> int:
    """Handle parse command."""
    logging.info(f"Parsing CDDL from {args.cddl_path}...")

    # Read CDDL file
    if args.cddl_path.is_dir():
        # Find .cddl files in directory
        cddl_files = list(args.cddl_path.glob("*.cddl"))
        if not cddl_files:
            logging.error(f"No CDDL files found in {args.cddl_path}")
            return 1
        cddl_file = cddl_files[0]
        logging.info(f"Found CDDL file: {cddl_file}")
    else:
        cddl_file = args.cddl_path

    if not cddl_file.exists():
        logging.error(f"CDDL file not found: {cddl_file}")
        return 1

    # Parse CDDL
    logging.info(f"Parsing {cddl_file}...")
    try:
        with open(cddl_file, "r") as f:
            cddl_text = f.read()
        
        spec = parser.parse_cddl(cddl_text)
        
        logging.info(f"Successfully parsed CDDL")
        logging.info(f"  Modules: {len(spec.modules)}")
        logging.info(f"  Types: {len(spec.types)}")
        
        # Show modules
        for module in spec.modules:
            logging.info(f"    Module '{module.name}': {len(module.types)} types")
        
        if args.output:
            logging.info(f"Output to JSON not yet implemented")
        
        return 0
    except SyntaxError as e:
        logging.error(f"Syntax error: {e}")
        return 1
    except Exception as e:
        logging.error(f"Parse error: {e}", exc_info=getattr(args, 'verbose', False))
        return 1


def cmd_generate(args) -> int:
    """Handle generate command."""
    logging.info(f"Generating Python code from {args.cddl_path}...")
    logging.info(f"Output directory: {args.output_dir}")
    logging.info(f"Strict mode: {args.strict}")

    # Read CDDL file
    if args.cddl_path.is_dir():
        # Find .cddl files in directory
        cddl_files = list(args.cddl_path.glob("*.cddl"))
        if not cddl_files:
            logging.error(f"No CDDL files found in {args.cddl_path}")
            return 1
        cddl_file = cddl_files[0]
        logging.info(f"Found CDDL file: {cddl_file}")
    else:
        cddl_file = args.cddl_path

    if not cddl_file.exists():
        logging.error(f"CDDL file not found: {cddl_file}")
        return 1

    # Parse CDDL
    logging.info(f"Parsing {cddl_file}...")
    try:
        with open(cddl_file, "r") as f:
            cddl_text = f.read()
        
        spec = parser.parse_cddl(cddl_text)
        logging.info(f"Successfully parsed CDDL ({len(spec.types)} types)")
        
        # Generate code
        logging.info(f"Generating Python code...")
        module_writer = writer.ModuleWriter(args.output_dir, strict_mode=args.strict)
        written_files = module_writer.write_specification(spec)
        
        logging.info(f"Generated {len(written_files)} modules:")
        for module_name, output_path in sorted(written_files.items()):
            logging.info(f"  - {module_name}: {output_path}")
        
        # Write __init__.py
        module_writer.write_init_file([m.name for m in spec.modules])
        
        return 0
    except SyntaxError as e:
        logging.error(f"Syntax error: {e}")
        return 1
    except Exception as e:
        logging.error(f"Generation error: {e}", exc_info=getattr(args, 'verbose', False))
        return 1


def cmd_generate_all(args) -> int:
    """Handle full workflow."""
    logging.info("Running full workflow: download -> parse -> generate")

    try:
        # Download
        logging.info("Step 1: Downloading CDDL specification...")
        cddl_dir = downloader.download_cddl_spec(output_dir=args.cddl_output_dir)
        logging.info(f"CDDL files downloaded to {cddl_dir}")

        # Parse
        logging.info("Step 2: Parsing CDDL files...")
        cddl_files = downloader.find_cddl_files(cddl_dir)
        logging.info(f"Found {len(cddl_files)} CDDL files")
        
        if not cddl_files:
            logging.error("No CDDL files found to parse")
            return 1
        
        # Parse first CDDL file
        cddl_file = cddl_files[0]
        logging.info(f"Parsing {cddl_file}...")
        with open(cddl_file, "r") as f:
            cddl_text = f.read()
        
        spec = parser.parse_cddl(cddl_text)
        logging.info(f"Parsed successfully ({len(spec.types)} types)")
        
        # Generate
        logging.info("Step 3: Generating Python code...")
        module_writer = writer.ModuleWriter(args.output_dir, strict_mode=args.strict)
        written_files = module_writer.write_specification(spec)
        
        logging.info(f"Generated {len(written_files)} modules:")
        for module_name in sorted(written_files.keys()):
            logging.info(f"  - {module_name}")
        
        # Write __init__.py
        module_writer.write_init_file([m.name for m in spec.modules])
        
        logging.info("Full workflow completed successfully!")
        return 0
        
    except Exception as e:
        logging.error(f"Workflow error: {e}", exc_info=getattr(args, 'verbose', False))
        return 1


if __name__ == "__main__":
    exit(main())
