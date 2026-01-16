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

"""Type guard functions for runtime validation during deserialization."""

from typing import Any


def is_string(value: Any) -> bool:
    """Check if value is a string."""
    return isinstance(value, str)


def is_number(value: Any) -> bool:
    """Check if value is a number (int or float)."""
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def is_boolean(value: Any) -> bool:
    """Check if value is a boolean."""
    return isinstance(value, bool)


def is_dict(value: Any) -> bool:
    """Check if value is a dictionary."""
    return isinstance(value, dict)


def is_list(value: Any) -> bool:
    """Check if value is a list."""
    return isinstance(value, list)


def is_none(value: Any) -> bool:
    """Check if value is None."""
    return value is None


def validate_required_field(
    json_dict: dict[str, Any],
    field_name: str,
    expected_type: type,
    camel_case: str | None = None,
) -> Any:
    """Validate and extract a required field from JSON.

    Args:
        json_dict: Dictionary containing field
        field_name: Python field name (snake_case)
        expected_type: Expected Python type
        camel_case: Camel-case key name in JSON (defaults to snake_case)

    Returns:
        Value if present and correct type

    Raises:
        ValueError: If field missing or wrong type
    """
    key = camel_case or _snake_to_camel(field_name)

    if key not in json_dict:
        raise ValueError(f"Required field '{key}' missing")

    value = json_dict[key]

    if not _is_type_match(value, expected_type):
        raise ValueError(
            f"Field '{key}' has wrong type. Expected {expected_type}, got {type(value)}"
        )

    return value


def validate_optional_field(
    json_dict: dict[str, Any],
    field_name: str,
    expected_type: type,
    default: Any = None,
    camel_case: str | None = None,
) -> Any:
    """Validate and extract an optional field from JSON.

    Args:
        json_dict: Dictionary containing field
        field_name: Python field name (snake_case)
        expected_type: Expected Python type
        default: Default value if not present
        camel_case: Camel-case key name in JSON (defaults to snake_to_camel)

    Returns:
        Value if present and correct type, otherwise default
    """
    key = camel_case or _snake_to_camel(field_name)

    if key not in json_dict:
        return default

    value = json_dict[key]

    if value is None:
        return None

    if not _is_type_match(value, expected_type):
        raise ValueError(
            f"Field '{key}' has wrong type. Expected {expected_type}, got {type(value)}"
        )

    return value


def validate_object(
    json_dict: dict[str, Any],
    required_fields: dict[str, type],
    optional_fields: dict[str, type] | None = None,
    lenient: bool = True,
) -> dict[str, Any]:
    """Validate object fields with lenient or strict mode.

    Args:
        json_dict: Dictionary to validate
        required_fields: Dict mapping field names to types
        optional_fields: Dict mapping optional field names to types
        lenient: If True, ignore unknown fields; if False, raise on unknown fields

    Returns:
        Validated dictionary with only known fields

    Raises:
        ValueError: If validation fails (type mismatch, missing required field)
        KeyError: If strict mode and unknown field present
    """
    if not is_dict(json_dict):
        raise ValueError(f"Expected dict, got {type(json_dict)}")

    optional_fields = optional_fields or {}

    # Validate required fields
    for field_name, field_type in required_fields.items():
        if field_name not in json_dict:
            raise ValueError(f"Required field '{field_name}' missing")

        value = json_dict[field_name]
        if not _is_type_match(value, field_type):
            raise ValueError(
                f"Field '{field_name}' has wrong type. Expected {field_type}, got {type(value)}"
            )

    # Validate optional fields
    known_keys = set(required_fields.keys()) | set(optional_fields.keys())
    for field_name, field_type in optional_fields.items():
        if field_name in json_dict:
            value = json_dict[field_name]
            if value is not None and not _is_type_match(value, field_type):
                raise ValueError(
                    f"Field '{field_name}' has wrong type. Expected {field_type}, got {type(value)}"
                )

    # Check for unknown fields
    if not lenient:
        unknown = set(json_dict.keys()) - known_keys
        if unknown:
            raise KeyError(f"Unknown fields in object: {unknown}")

    # Return validated object (only known fields)
    result = {}
    for key, value in json_dict.items():
        if key in known_keys:
            result[key] = value
    return result


def _is_type_match(value: Any, expected_type: type) -> bool:
    """Check if value matches expected type.

    Supports:
    - Basic types: str, int, float, bool, dict, list
    - Optional types: Optional[T]
    - Union types: T1 | T2
    """
    if expected_type is Any:
        return True

    # Handle None
    if value is None:
        return expected_type is type(None) or _is_optional(expected_type)

    # Handle Optional
    if _is_optional(expected_type):
        inner_type = _get_optional_inner_type(expected_type)
        return isinstance(value, inner_type) or value is None

    # Handle Union
    if hasattr(expected_type, "__args__"):
        return isinstance(value, expected_type.__args__)

    # Basic type check
    return isinstance(value, expected_type)


def _is_optional(type_hint: Any) -> bool:
    """Check if type hint is Optional[T] (Union with None)."""
    return (
        hasattr(type_hint, "__args__")
        and type(None) in type_hint.__args__
    )


def _get_optional_inner_type(type_hint: Any) -> tuple:
    """Extract inner type from Optional[T]."""
    args = [arg for arg in type_hint.__args__ if arg is not type(None)]
    return tuple(args) if len(args) > 1 else args[0]


def _snake_to_camel(snake_str: str) -> str:
    """Convert snake_case to camelCase.

    Examples:
        'session_id' -> 'sessionId'
        'browsing_context' -> 'browsingContext'
    """
    components = snake_str.split("_")
    return components[0] + "".join(x.title() for x in components[1:])
