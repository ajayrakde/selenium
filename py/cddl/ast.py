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

"""Abstract Syntax Tree (AST) data structures for CDDL definitions."""

from dataclasses import dataclass, field
from typing import Optional, List, Union


@dataclass
class CddlField:
    """Represents a field in a CDDL object type."""
    name: str
    type: "CddlType"
    optional: bool = False
    description: Optional[str] = None


@dataclass
class CddlType:
    """Base class for all CDDL type definitions."""
    name: str
    base_type: str = "unknown"  # Type category: ref, string, number, etc.
    module: Optional[str] = None  # e.g., "session", "network"
    description: Optional[str] = None
    reference: Optional[str] = None  # For type references
    value: Optional[str] = None  # For literal values
    is_primitive: bool = False  # Whether this is a primitive type


@dataclass
class CddlPrimitiveType(CddlType):
    """Represents a primitive CDDL type (text, bool, number, etc.)."""
    python_type: str = "Any"  # Maps to Python type


@dataclass
class CddlObject(CddlType):
    """Represents a CDDL object type with fields."""
    fields: List[CddlField] = field(default_factory=list)
    extensible: bool = False  # Whether object allows additional fields


@dataclass
class CddlEnum(CddlType):
    """Represents a CDDL enum type with string values."""
    values: List[str] = field(default_factory=list)


@dataclass
class CddlUnionVariant:
    """Represents one variant in a union type."""
    index: int = 0  # Index in union
    type: Optional[CddlType] = None  # The type for this variant
    discriminator_field: Optional[str] = None  # For tagged unions
    discriminator_value: Optional[str] = None  # Value of discriminator field


@dataclass
class CddlUnion(CddlType):
    """Represents a CDDL union type (multiple possible types)."""
    variants: List[CddlUnionVariant] = field(default_factory=list)


@dataclass
class CddlArray(CddlType):
    """Represents a CDDL array type."""
    item_type: Optional[CddlType] = None


@dataclass
class CddlCommand(CddlType):
    """Represents a WebDriver BiDi command."""
    method: str = ""  # e.g., "session.new"
    params: Optional[CddlObject] = None
    result: Optional[CddlObject] = None


@dataclass
class CddlModule:
    """Represents a module grouping (e.g., all session.* types)."""
    name: str  # e.g., "session", "network"
    types: dict = field(default_factory=dict)  # Dict[name, CddlType]
    commands: List[CddlCommand] = field(default_factory=list)


@dataclass
class CddlSpecification:
    """Root node representing entire CDDL specification."""
    modules: List[CddlModule] = field(default_factory=list)
    types: dict = field(default_factory=dict)  # Dict[name, CddlType] - Global types
