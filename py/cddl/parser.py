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

"""CDDL (RFC 8610) lexer and parser for WebDriver BiDi specification."""

from dataclasses import dataclass
from enum import Enum, auto
from typing import Any

from . import ast


class TokenType(Enum):
    """Token types for CDDL lexer."""
    # Literals
    IDENTIFIER = auto()
    STRING = auto()
    NUMBER = auto()
    RANGE = auto()

    # Operators
    ASSIGN = auto()  # =
    UNION = auto()  # /
    QUESTION = auto()  # ?
    DOUBLE_DOT = auto()  # ..
    ARROW = auto()  # =>
    PLUS = auto()  # + (one or more)

    # Delimiters
    COLON = auto()  # :
    COMMA = auto()  # ,
    LBRACE = auto()  # {
    RBRACE = auto()  # }
    LBRACKET = auto()  # [
    RBRACKET = auto()  # ]
    LPAREN = auto()  # (
    RPAREN = auto()  # )
    STAR = auto()  # *

    # Keywords
    EXTENSIBLE = auto()

    # Special
    COMMENT = auto()
    EOF = auto()


@dataclass
class Token:
    """Represents a lexical token."""
    type: TokenType
    value: str
    line: int
    column: int


class CddlLexer:
    """Lexer for tokenizing CDDL specifications."""

    def __init__(self, text: str):
        """Initialize lexer with CDDL source text.

        Args:
            text: CDDL specification text
        """
        self.text = text
        self.pos = 0
        self.line = 1
        self.column = 1
        self.tokens: list[Token] = []

    def tokenize(self) -> list[Token]:
        """Tokenize the CDDL text.

        Returns:
            List of tokens
        """
        while self.pos < len(self.text):
            self._skip_whitespace()
            if self.pos >= len(self.text):
                break

            # Skip comments
            if self._peek() == ";":
                self._skip_comment()
                continue

            # Single character tokens
            char = self._peek()
            if char == "=":
                # Check for =>
                if self._peek_ahead(1) == ">":
                    self.tokens.append(Token(TokenType.ARROW, "=>", self.line, self.column))
                    self._advance()
                    self._advance()
                else:
                    self.tokens.append(self._make_token(TokenType.ASSIGN))
                    self._advance()
            elif char == "/":
                # Check for //
                if self._peek_ahead(1) == "/":
                    self.tokens.append(Token(TokenType.UNION, "//", self.line, self.column))
                    self._advance()
                    self._advance()
                else:
                    self.tokens.append(self._make_token(TokenType.UNION))
                    self._advance()
            elif char == "?":
                self.tokens.append(self._make_token(TokenType.QUESTION))
                self._advance()
            elif char == ":":
                self.tokens.append(self._make_token(TokenType.COLON))
                self._advance()
            elif char == ",":
                self.tokens.append(self._make_token(TokenType.COMMA))
                self._advance()
            elif char == "{":
                self.tokens.append(self._make_token(TokenType.LBRACE))
                self._advance()
            elif char == "}":
                self.tokens.append(self._make_token(TokenType.RBRACE))
                self._advance()
            elif char == "[":
                self.tokens.append(self._make_token(TokenType.LBRACKET))
                self._advance()
            elif char == "]":
                self.tokens.append(self._make_token(TokenType.RBRACKET))
                self._advance()
            elif char == "(":
                self.tokens.append(self._make_token(TokenType.LPAREN))
                self._advance()
            elif char == ")":
                self.tokens.append(self._make_token(TokenType.RPAREN))
                self._advance()
            elif char == "*":
                self.tokens.append(self._make_token(TokenType.STAR))
                self._advance()
            elif char == "+":
                self.tokens.append(self._make_token(TokenType.PLUS))
                self._advance()
            elif char == ".":
                if self._peek_ahead(1) == ".":
                    self.tokens.append(self._make_token(TokenType.DOUBLE_DOT))
                    self._advance()
                    self._advance()
                else:
                    # Dot in identifier
                    token = self._read_identifier()
                    self.tokens.append(token)
            elif char == '"':
                token = self._read_string()
                self.tokens.append(token)
            elif char.isdigit() or (char == "-" and self._peek_ahead(1).isdigit()):
                token = self._read_number()
                self.tokens.append(token)
            elif char.isalpha() or char == "_":
                token = self._read_identifier()
                self.tokens.append(token)
            else:
                raise SyntaxError(
                    f"Unexpected character '{char}' at line {self.line}, column {self.column}"
                )

        self.tokens.append(Token(TokenType.EOF, "", self.line, self.column))
        return self.tokens

    def _peek(self, offset: int = 0) -> str:
        """Peek at character at current position + offset."""
        pos = self.pos + offset
        return self.text[pos] if pos < len(self.text) else ""

    def _peek_ahead(self, n: int) -> str:
        """Peek n characters ahead."""
        return self._peek(n)

    def _advance(self):
        """Advance position and update line/column."""
        if self.pos < len(self.text):
            if self.text[self.pos] == "\n":
                self.line += 1
                self.column = 1
            else:
                self.column += 1
            self.pos += 1

    def _skip_whitespace(self):
        """Skip whitespace characters."""
        while self.pos < len(self.text) and self.text[self.pos] in " \t\n\r":
            self._advance()

    def _skip_comment(self):
        """Skip comment until end of line."""
        while self.pos < len(self.text) and self.text[self.pos] != "\n":
            self._advance()

    def _read_string(self) -> Token:
        """Read a string literal."""
        start_line = self.line
        start_col = self.column
        self._advance()  # Skip opening quote

        value = ""
        while self.pos < len(self.text) and self._peek() != '"':
            if self._peek() == "\\":
                self._advance()
                if self.pos < len(self.text):
                    value += self._peek()
                    self._advance()
            else:
                value += self._peek()
                self._advance()

        if self.pos >= len(self.text):
            raise SyntaxError(f"Unterminated string at line {start_line}, column {start_col}")

        self._advance()  # Skip closing quote
        return Token(TokenType.STRING, value, start_line, start_col)

    def _read_identifier(self) -> Token:
        """Read an identifier or keyword."""
        start_line = self.line
        start_col = self.column

        value = ""
        while (
            self.pos < len(self.text)
            and (self._peek().isalnum() or self._peek() in "._-")
        ):
            value += self._peek()
            self._advance()

        # Check for keyword
        if value == "Extensible":
            return Token(TokenType.EXTENSIBLE, value, start_line, start_col)

        return Token(TokenType.IDENTIFIER, value, start_line, start_col)

    def _read_number(self) -> Token:
        """Read a number (possibly with range)."""
        start_line = self.line
        start_col = self.column

        # Read first number
        value = ""
        if self._peek() == "-":
            value += "-"
            self._advance()

        while self.pos < len(self.text) and self._peek().isdigit():
            value += self._peek()
            self._advance()

        # Check for range notation
        if self._peek() == "." and self._peek_ahead(1) == ".":
            value += ".."
            self._advance()
            self._advance()
            # Read end number
            if self._peek() == "-":
                value += "-"
                self._advance()
            while self.pos < len(self.text) and self._peek().isdigit():
                value += self._peek()
                self._advance()
            return Token(TokenType.RANGE, value, start_line, start_col)

        return Token(TokenType.NUMBER, value, start_line, start_col)

    def _make_token(self, token_type: TokenType) -> Token:
        """Create a token at current position."""
        return Token(token_type, self._peek(), self.line, self.column)


class CddlParser:
    """Parser for CDDL specifications."""

    def __init__(self, tokens: list[Token]):
        """Initialize parser with token stream.

        Args:
            tokens: List of tokens from lexer
        """
        self.tokens = tokens
        self.pos = 0

    def parse(self) -> dict:
        """Parse CDDL token stream.

        Returns:
            Dictionary of parsed CDDL definitions
        """
        definitions = {}
        while not self._is_at_end():
            try:
                definition = self._parse_definition()
                if definition:
                    name, value = definition
                    definitions[name] = value
                else:
                    # Skip tokens if parse_definition returned None
                    # This prevents infinite loops
                    self._advance()
            except SyntaxError:
                # Skip to next definition on error
                # Look for next IDENTIFIER or EXTENSIBLE followed by ASSIGN
                while not self._is_at_end():
                    if (self._current_token().type in (TokenType.IDENTIFIER, TokenType.EXTENSIBLE) and
                        self._peek_token(1).type == TokenType.ASSIGN):
                        # Found start of next definition
                        break
                    self._advance()
        return definitions

    def _current_token(self) -> Token:
        """Get current token."""
        return self.tokens[self.pos] if self.pos < len(self.tokens) else self.tokens[-1]

    def _peek_token(self, offset: int = 1) -> Token:
        """Peek ahead at token."""
        pos = self.pos + offset
        return self.tokens[pos] if pos < len(self.tokens) else self.tokens[-1]

    def _advance(self):
        """Advance to next token."""
        if not self._is_at_end():
            self.pos += 1

    def _is_at_end(self) -> bool:
        """Check if at end of tokens."""
        return self._current_token().type == TokenType.EOF

    def _expect(self, token_type: TokenType) -> Token:
        """Consume token of expected type."""
        token = self._current_token()
        if token.type != token_type:
            raise SyntaxError(
                f"Expected {token_type}, got {token.type} at line {token.line}, column {token.column}"
            )
        self._advance()
        return token

    def _parse_definition(self) -> tuple | None:
        """Parse a CDDL type definition."""
        if self._is_at_end():
            return None

        token = self._current_token()
        # Allow IDENTIFIER or EXTENSIBLE as definition names
        if token.type not in (TokenType.IDENTIFIER, TokenType.EXTENSIBLE):
            return None

        name = token.value
        self._advance()

        self._expect(TokenType.ASSIGN)
        value = self._parse_value()

        return name, value

    def _parse_value(self) -> dict:
        """Parse a CDDL value (type definition)."""
        return self._parse_union()

    def _parse_union(self) -> dict:
        """Parse union types (Type1 / Type2)."""
        left = self._parse_primary()

        while self._current_token().type == TokenType.UNION:
            self._advance()
            right = self._parse_primary()
            left = {"type": "union", "variants": [left, right]}

        return left

    def _parse_primary(self) -> dict:
        """Parse primary type expression."""
        token = self._current_token()

        # Object type
        if token.type == TokenType.LBRACE:
            return self._parse_object()

        # Array type or map
        if token.type == TokenType.LBRACKET:
            return self._parse_array()

        # Map syntax (*KeyType => ValueType) or array element count
        if token.type == TokenType.STAR:
            self._advance()
            # Could be:
            # 1. Map syntax: *KeyType => ValueType  
            # 2. Array element count: *ItemType
            # Try to parse what follows
            key_or_item = self._parse_primary()
            
            # Check for arrow (map syntax)
            if self._current_token().type == TokenType.ARROW:
                self._advance()
                value_type = self._parse_value()
                # Return as map type
                return {"type": "map", "key_type": key_or_item, "value_type": value_type}
            else:
                # Just a prefixed item type (array element with count)
                return {"type": "array", "item_type": key_or_item}

        # Group
        if token.type == TokenType.LPAREN:
            self._advance()
            value = self._parse_value()
            self._expect(TokenType.RPAREN)
            return value

        # String or number literal (enum values)
        if token.type == TokenType.STRING:
            self._advance()
            return {"type": "string_literal", "value": token.value}

        if token.type == TokenType.NUMBER:
            self._advance()
            return {"type": "number_literal", "value": token.value}

        if token.type == TokenType.RANGE:
            self._advance()
            return {"type": "range", "value": token.value}

        # Type reference
        if token.type == TokenType.IDENTIFIER:
            self._advance()
            return {"type": "ref", "name": token.value}

        raise SyntaxError(
            f"Unexpected token {token.type} at line {token.line}, column {token.column}"
        )

    def _parse_object(self) -> dict:
        """Parse object type { field: type, ... }."""
        self._expect(TokenType.LBRACE)

        fields = []
        extensible = False
        is_union_type = False  # If we see unions, this is a union-like object

        while self._current_token().type != TokenType.RBRACE:
            # Check for optional field
            optional = False
            if self._current_token().type == TokenType.QUESTION:
                optional = True
                self._advance()

            # Check for Extensible marker
            if self._current_token().type == TokenType.EXTENSIBLE:
                extensible = True
                self._advance()
                if self._current_token().type == TokenType.COMMA:
                    self._advance()
                continue

            # Field name or inline type
            if self._current_token().type not in (TokenType.IDENTIFIER, TokenType.EXTENSIBLE):
                # Skip commas and unions at object level
                if self._current_token().type in (TokenType.COMMA, TokenType.UNION):
                    self._advance()
                    continue
                break

            name_token = self._current_token()
            name = name_token.value
            self._advance()

            # Check if this has a type annotation or is just a type reference
            if self._current_token().type == TokenType.COLON:
                # Named field: name: type
                self._advance()
                field_type = self._parse_union()
                fields.append({"name": name, "type": field_type, "optional": optional})
            elif self._current_token().type == TokenType.UNION:
                # Union at object level: this is a union type, not a field
                is_union_type = True
                field_type = {"type": "ref", "name": name}
                fields.append({"name": name, "type": field_type, "optional": optional})
                # Don't consume the union, let the outer loop handle it
            else:
                # Inline type reference: just the type name (no colon)
                # Treat as {name: name} where name is both field and type
                field_type = {"type": "ref", "name": name}
                fields.append({"name": name, "type": field_type, "optional": optional})

            # Check for comma or end
            if self._current_token().type == TokenType.COMMA:
                self._advance()

            # Check for Extensible marker
            if self._current_token().type == TokenType.EXTENSIBLE:
                extensible = True
                self._advance()
                if self._current_token().type == TokenType.COMMA:
                    self._advance()

        self._expect(TokenType.RBRACE)

        return {"type": "object", "fields": fields, "extensible": extensible}

    def _parse_array(self) -> dict:
        """Parse array type [*ItemType] or [+ItemType] or map (*KeyType => ValueType)."""
        self._expect(TokenType.LBRACKET)

        # Check for occurrences marker
        occurs_marker = None  # "*" or "+" for * or +
        if self._current_token().type == TokenType.STAR:
            occurs_marker = "*"
            self._advance()
        elif self._current_token().type == TokenType.PLUS:
            occurs_marker = "+"
            self._advance()

        # Try to parse as map if we saw "*"
        if occurs_marker == "*" and self._current_token().type == TokenType.IDENTIFIER:
            start_pos = self.pos
            key_name = self._current_token().value
            self._advance()
            key_type = {"type": "ref", "name": key_name}
            
            # Check for => arrow (map syntax)
            if self._current_token().type == TokenType.ARROW:
                self._advance()
                value_type = self._parse_value()
                self._expect(TokenType.RBRACKET)
                # Return as extensible/map type
                return {"type": "map", "key_type": key_type, "value_type": value_type}
            # Not a map, reset and parse as array
            self.pos = start_pos

        # Parse item type
        item_type = self._parse_value()
        self._expect(TokenType.RBRACKET)

        return {
            "type": "array",
            "item_type": item_type,
            "occurs": occurs_marker,  # "*" for zero-or-more, "+" for one-or-more
        }


class AstTransformer:
    """Transform parsed CDDL dict intermediate representation to AST objects."""

    def __init__(self, definitions: dict[str, Any]):
        """Initialize transformer with parsed definitions.

        Args:
            definitions: Dictionary of parsed CDDL definitions from CddlParser
        """
        self.definitions = definitions
        self.cddl_types: dict[str, ast.CddlType] = {}
        self.modules: dict[str, ast.CddlModule] = {}

    def transform(self) -> ast.CddlSpecification:
        """Transform parsed definitions to CddlSpecification AST.

        Returns:
            CddlSpecification containing all modules and types
        """
        # First pass: collect all type definitions
        for name, definition in self.definitions.items():
            cddl_type = self._transform_type(name, definition)
            if cddl_type:
                self.cddl_types[name] = cddl_type

        # Second pass: organize types into modules
        self._organize_modules()

        # Create specification
        modules = list(self.modules.values())
        return ast.CddlSpecification(modules=modules, types=self.cddl_types)

    def _organize_modules(self):
        """Organize types into modules based on naming conventions.

        Types starting with "module." are grouped into modules.
        Types without a module prefix are placed in global module.
        """
        global_types = {}

        for name, cddl_type in self.cddl_types.items():
            if "." in name:
                # Extract module name from dotted identifier
                module_name = name.split(".")[0]
                if module_name not in self.modules:
                    self.modules[module_name] = ast.CddlModule(
                        name=module_name, types={}, commands=[]
                    )
                self.modules[module_name].types[name] = cddl_type
            else:
                # Global type (no module prefix)
                global_types[name] = cddl_type

        # Create global module if there are global types
        if global_types:
            self.modules["_global"] = ast.CddlModule(
                name="_global", types=global_types, commands=[]
            )

    def _transform_type(self, name: str, definition: Any) -> ast.CddlType | None:
        """Transform a single type definition to AST.

        Args:
            name: Type name
            definition: Definition dict from parser

        Returns:
            CddlType object or None if invalid
        """
        if not isinstance(definition, dict):
            return None

        type_kind = definition.get("type")

        if type_kind == "object":
            return self._transform_object(name, definition)
        elif type_kind == "union":
            return self._transform_union(name, definition)
        elif type_kind == "array":
            return self._transform_array_type(name, definition)
        elif type_kind == "ref":
            # Simple type reference
            return ast.CddlType(
                name=name,
                base_type="ref",
                reference=definition.get("name"),
                is_primitive=False,
            )
        elif type_kind == "string_literal":
            # Enum variant
            return ast.CddlType(
                name=name,
                base_type="string",
                value=definition.get("value"),
                is_primitive=True,
            )
        elif type_kind == "number_literal":
            # Number variant
            return ast.CddlType(
                name=name,
                base_type="number",
                value=definition.get("value"),
                is_primitive=True,
            )
        elif type_kind == "range":
            # Number range
            return ast.CddlType(
                name=name,
                base_type="number_range",
                value=definition.get("value"),
                is_primitive=True,
            )

        return None

    def _transform_object(self, name: str, definition: dict) -> ast.CddlObject:
        """Transform object type definition to CddlObject.

        Args:
            name: Object name
            definition: Object definition dict

        Returns:
            CddlObject AST node
        """
        fields = []
        for field_def in definition.get("fields", []):
            field_type = self._transform_value(field_def.get("type"))
            field = ast.CddlField(
                name=field_def.get("name"),
                type=field_type,
                optional=field_def.get("optional", False),
                description=None,
            )
            fields.append(field)

        return ast.CddlObject(
            name=name,
            fields=fields,
            extensible=definition.get("extensible", False),
            description=None,
        )

    def _transform_union(self, name: str, definition: dict) -> ast.CddlUnion:
        """Transform union type definition to CddlUnion.

        Args:
            name: Union name
            definition: Union definition dict

        Returns:
            CddlUnion AST node
        """
        variants = []
        variant_defs = self._flatten_union_variants(definition)

        for i, variant_def in enumerate(variant_defs):
            variant_type = self._transform_value(variant_def)
            variant = ast.CddlUnionVariant(
                index=i,
                type=variant_type,
                discriminator_field=None,
                discriminator_value=None,
            )
            variants.append(variant)

        return ast.CddlUnion(
            name=name, variants=variants, description=None
        )

    def _flatten_union_variants(self, definition: dict) -> list[Any]:
        """Flatten nested union definitions into a list of variants.

        Args:
            definition: Union definition possibly containing nested unions

        Returns:
            List of variant definitions
        """
        if definition.get("type") != "union":
            return [definition]

        variants = []
        # Recursively flatten
        for variant in definition.get("variants", []):
            if variant.get("type") == "union":
                variants.extend(self._flatten_union_variants(variant))
            else:
                variants.append(variant)
        return variants

    def _transform_array_type(
        self, name: str, definition: dict
    ) -> ast.CddlArray:
        """Transform array type definition to CddlArray.

        Args:
            name: Array type name
            definition: Array definition dict

        Returns:
            CddlArray AST node
        """
        item_type = self._transform_value(definition.get("item_type"))
        return ast.CddlArray(name=name, item_type=item_type, description=None)

    def _transform_value(self, value: Any) -> ast.CddlType:
        """Transform a value dict to CddlType.

        Args:
            value: Value definition dict

        Returns:
            CddlType object
        """
        if not isinstance(value, dict):
            # Fallback for unexpected types
            return ast.CddlType(
                name="unknown", base_type="unknown", is_primitive=False
            )

        type_kind = value.get("type")

        if type_kind == "ref":
            return ast.CddlType(
                name=value.get("name", "unknown"),
                base_type="ref",
                reference=value.get("name"),
                is_primitive=False,
            )
        elif type_kind == "map":
            # Map/extensible fields (*KeyType => ValueType)
            # Treat as generic dict-like type
            return ast.CddlType(
                name="", base_type="map", is_primitive=False
            )
        elif type_kind == "object":
            fields = []
            for field_def in value.get("fields", []):
                field_type = self._transform_value(field_def.get("type"))
                field = ast.CddlField(
                    name=field_def.get("name"),
                    type=field_type,
                    optional=field_def.get("optional", False),
                    description=None,
                )
                fields.append(field)
            return ast.CddlObject(
                name="", fields=fields, extensible=value.get("extensible", False), description=None
            )
        elif type_kind == "array":
            item_type = self._transform_value(value.get("item_type"))
            return ast.CddlArray(name="", item_type=item_type, description=None)
        elif type_kind == "union":
            variants = []
            variant_defs = self._flatten_union_variants(value)
            for i, variant_def in enumerate(variant_defs):
                variant_type = self._transform_value(variant_def)
                variant = ast.CddlUnionVariant(
                    index=i,
                    type=variant_type,
                    discriminator_field=None,
                    discriminator_value=None,
                )
                variants.append(variant)
            return ast.CddlUnion(name="", variants=variants, description=None)
        elif type_kind == "string_literal":
            return ast.CddlType(
                name="",
                base_type="string",
                value=value.get("value"),
                is_primitive=True,
            )
        elif type_kind == "number_literal":
            return ast.CddlType(
                name="",
                base_type="number",
                value=value.get("value"),
                is_primitive=True,
            )
        elif type_kind == "range":
            return ast.CddlType(
                name="",
                base_type="number_range",
                value=value.get("value"),
                is_primitive=True,
            )

        return ast.CddlType(
            name="unknown", base_type="unknown", is_primitive=False
        )


def parse_cddl(text: str) -> ast.CddlSpecification:
    """Parse CDDL text and return AST specification.

    Args:
        text: CDDL source code

    Returns:
        CddlSpecification AST object

    Raises:
        SyntaxError: If CDDL syntax is invalid
    """
    lexer = CddlLexer(text)
    tokens = lexer.tokenize()

    parser = CddlParser(tokens)
    definitions = parser.parse()

    transformer = AstTransformer(definitions)
    return transformer.transform()

