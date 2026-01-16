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

"""Download and extract W3C WebDriver BiDi CDDL specification."""

import re
from pathlib import Path
from urllib.error import URLError
from urllib.request import urlopen

# W3C WebDriver BiDi specification URL with CDDL index
W3C_WEBDRIVER_BIDI_URL = "https://www.w3.org/TR/webdriver-bidi/"


def download_cddl_spec(
    output_dir: Path | None = None,
    force: bool = False,
) -> Path:
    """Download W3C WebDriver BiDi CDDL specification from HTML spec.

    Args:
        output_dir: Directory to save CDDL file to. Defaults to py/cddl/specs/
        force: Force re-download even if files already exist

    Returns:
        Path to directory containing extracted CDDL file

    Raises:
        URLError: If download fails
        ValueError: If CDDL section not found in spec
    """
    if output_dir is None:
        # Default to py/cddl/specs/
        output_dir = Path(__file__).parent / "specs"

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Check if CDDL file already exists
    cddl_file = output_dir / "webdriver-bidi.cddl"
    if cddl_file.exists() and not force:
        print(f"CDDL file already exists: {cddl_file}")
        return output_dir

    # Download spec
    print(f"Downloading W3C WebDriver BiDi specification from {W3C_WEBDRIVER_BIDI_URL}...")
    try:
        with urlopen(W3C_WEBDRIVER_BIDI_URL) as response:
            spec_html = response.read().decode("utf-8")
        print("Downloaded specification")
    except URLError as e:
        raise URLError(f"Failed to download W3C spec: {e}")

    # Extract CDDL from HTML
    print("Extracting CDDL from specification...")
    cddl_content = _extract_cddl_from_html(spec_html)

    if not cddl_content:
        raise ValueError(
            "Could not find CDDL section in specification. "
            "Specification format may have changed."
        )

    # Write CDDL to file
    with open(cddl_file, "w", encoding="utf-8") as f:
        f.write(cddl_content)

    print(f"Saved CDDL to {cddl_file}")
    return output_dir


def _extract_cddl_from_html(html_content: str) -> str:
    """Extract CDDL definitions from W3C spec HTML.

    Looks for CDDL definitions in <pre> tags within the CDDL Index section.

    Args:
        html_content: HTML content of the W3C spec

    Returns:
        CDDL text content, or empty string if not found
    """
    # Find the CDDL Index section
    cddl_start_marker = 'id="cddl-index-cddl-module-remote-end-definition"'
    cddl_end_marker = 'id="cddl-index-cddl-module-local-end-definition"'

    start_idx = html_content.find(cddl_start_marker)
    end_idx = html_content.find(cddl_end_marker)

    if start_idx == -1 or end_idx == -1:
        # Fallback: look for any CDDL Index section
        if "CDDL Index" not in html_content:
            return ""
        # Just use everything after "CDDL Index"
        start_idx = html_content.find("CDDL Index")
        end_idx = len(html_content)
    else:
        # Start from remote end definition, go to local end definition
        pass

    cddl_section = html_content[start_idx:end_idx]

    # Extract text from <pre> tags (these contain the actual CDDL)
    pre_pattern = r"<pre[^>]*>(.*?)</pre>"
    pre_matches = re.findall(pre_pattern, cddl_section, re.DOTALL)

    if not pre_matches:
        return ""

    # Join all pre blocks with newlines
    cddl_text = "\n".join(pre_matches)

    # Clean up HTML artifacts
    # Remove HTML tags, decode entities
    cddl_text = re.sub(r"<[^>]+>", "", cddl_text)
    cddl_text = re.sub(r"&lt;", "<", cddl_text)
    cddl_text = re.sub(r"&gt;", ">", cddl_text)
    cddl_text = re.sub(r"&amp;", "&", cddl_text)
    cddl_text = re.sub(r"&quot;", '"', cddl_text)
    cddl_text = re.sub(r"&apos;", "'", cddl_text)
    cddl_text = re.sub(r"&#8610;", "↪", cddl_text)
    cddl_text = re.sub(r"&nbsp;", " ", cddl_text)

    # Clean up whitespace: remove excessive blank lines but preserve structure
    lines = [line.rstrip() for line in cddl_text.split("\n")]
    lines = [line for line in lines if line.strip()]

    return "\n".join(lines)


def find_cddl_files(directory: Path) -> list:
    """Find all .cddl files in directory.

    Args:
        directory: Directory to search

    Returns:
        List of .cddl file paths
    """
    return sorted(directory.glob("**/*.cddl"))


def read_cddl_file(file_path: Path) -> str:
    """Read CDDL file content.

    Args:
        file_path: Path to .cddl file

    Returns:
        File content as string
    """
    with open(file_path, encoding="utf-8") as f:
        return f.read()
