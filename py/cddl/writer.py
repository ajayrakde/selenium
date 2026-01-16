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

"""Module writer for generated CDDL code.

Handles writing generated Python code to files organized by module.
"""

import logging
from pathlib import Path

from . import ast
from .generator import PythonCodeGenerator


class ModuleWriter:
    """Writes generated code modules to disk."""

    def __init__(self, output_dir: Path, strict_mode: bool = False):
        """Initialize module writer.

        Args:
            output_dir: Directory to write generated modules to
            strict_mode: If True, use strict validation mode
        """
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.strict_mode = strict_mode
        self.generator = PythonCodeGenerator(strict_mode=strict_mode)
        self.logger = logging.getLogger(__name__)

    def write_specification(self, spec: ast.CddlSpecification) -> dict[str, Path]:
        """Write all modules from a specification.

        Args:
            spec: CddlSpecification AST node

        Returns:
            Dict mapping module names to written file paths
        """
        written_files = {}

        for module in spec.modules:
            if module.name == "_global":
                # Skip global module for now
                continue

            output_path = self._write_module(module)
            if output_path:
                written_files[module.name] = output_path
                self.logger.info(f"Generated {module.name}: {output_path}")

        return written_files

    def _write_module(self, module: ast.CddlModule) -> Path | None:
        """Write a single module to disk.

        Args:
            module: CddlModule AST node

        Returns:
            Path to written file or None on error
        """
        # Generate Python code for this module
        code = self.generator.generate_module(module)

        # Determine output filename
        output_file = self.output_dir / f"{module.name}.py"

        # Write to disk
        try:
            with open(output_file, "w") as f:
                f.write(code)
            return output_file
        except Exception as e:
            self.logger.error(f"Failed to write {output_file}: {e}")
            return None

    def write_init_file(self, modules: list) -> Path:
        """Write __init__.py file that imports all generated modules.

        Args:
            modules: List of module names

        Returns:
            Path to written __init__.py file
        """
        init_file = self.output_dir / "__init__.py"

        lines = [
            "# Auto-generated __init__.py from CDDL specification",
            "",
            "# Import all BiDi protocol types",
        ]

        # Import each module
        for module_name in sorted(modules):
            if module_name != "_global":
                lines.append(f"from . import {module_name}")

        lines.append("")
        lines.append("__all__ = [")
        for module_name in sorted(modules):
            if module_name != "_global":
                lines.append(f'    "{module_name}",')
        lines.append("]")

        content = "\n".join(lines)

        with open(init_file, "w") as f:
            f.write(content)

        self.logger.info(f"Generated __init__.py: {init_file}")
        return init_file
