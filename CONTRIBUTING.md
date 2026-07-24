# Contributing to NetComm-Pro

Thank you for considering contributing to **NetComm-Pro**! This document provides guidelines and instructions for contributing to this project.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contribution Workflow](#contribution-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

---

## 🤝 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone. We expect all contributors to:

- Use welcoming and inclusive language
- Respect differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what's best for the community
- Show empathy towards other contributors

### Unacceptable Behavior

Harassment, discriminatory language, or any unprofessional conduct is not tolerated.

## 🚀 Getting Started

### Prerequisites

- Python 3.8 or higher
- Java 11 or higher (for Java contributions)
- Git for version control
- Basic knowledge of networking concepts

### Resources to Read

1. `README.md` - Project overview
2. `docs/api/API_Reference.md` - API documentation
3. `docs/architecture/System_Design.md` - Architecture understanding

## 🛠️ Development Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/NetComm-Pro.git
cd NetComm-Pro
```

### 2. Set Up Virtual Environment

```bash
# Create virtual environment
python -m venv venv

# Activate it
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate   # Windows

# Install dependencies
pip install -r requirements.txt

# Install in development mode
pip install -e .

# Install development dependencies
pip install pytest pytest-cov black flake8 mypy
```

### 3. Set Up Pre-commit Hooks (Optional but Recommended)

```bash
pip install pre-commit
pre-commit install
```

### 4. Verify Installation

```bash
# Run tests to verify setup
pytest tests/ -v

# Run linting
flake8 src/python/socketcomm/
```

## 🔄 Contribution Workflow

### 1. Create a Branch

```bash
# Update main branch first
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/your-feature-name
# Or for bug fixes:
git checkout -b fix/bug-description
```

### Branch Naming Convention

| Type | Prefix | Example |
|------|--------|---------|
| Feature | `feature/` | `feature/udp-support` |
| Bug Fix | `fix/` | `fix/connection-timeout` |
| Documentation | `docs/` | `docs/update-api-ref` |
| Test | `test/` | `test/file-transfer-edge-cases` |
| Refactor | `refactor/` | `refactor/security-module` |

### 2. Make Your Changes

- Write clean, readable code
- Follow existing code style
- Add comments for complex logic
- Update documentation if needed

### 3. Test Your Changes

```bash
# Run all tests
pytest tests/ -v

# Run specific test file
pytest tests/test_socketcomm.py::TestSCMPProtocol -v

# Run with coverage
pytest tests/ --cov=socketcomm --cov-report=term-missing
```

### 4. Commit Your Changes

```bash
# Stage your changes
git add .

# Commit with descriptive message
git commit -m "feat: add UDP broadcast support for server discovery"
```

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/your-feature-name

# Open Pull Request on GitHub
```

## 📝 Coding Standards

### Python (PEP 8)

```python
# ✅ Good: Clear naming, docstrings, type hints
def send_message(self, message: str, timeout: float = 30.0) -> bool:
    """
    Send a message to the connected server.
    
    Args:
        message: The message string to send.
        timeout: Connection timeout in seconds.
        
    Returns:
        True if message was sent successfully.
    
    Raises:
        ConnectionError: If not connected to server.
    """
    if not self.is_connected():
        raise ConnectionError("Not connected to server")
    
    # Implementation here...
    return True


# ❌ Bad: Unclear names, no docs
def sm(m, t=30):
    # send msg
    if not self.c:
        raise Error("no conn")
    return True
```

### Code Formatting

We use **Black** for code formatting:

```bash
# Format code
black src/python/socketcomm/

# Check formatting without changing
black --check src/python/socketcomm/
```

### Linting

We use **Flake8** for linting:

```bash
# Run linter
flake8 src/python/socketcomm/ --max-line-length=100
```

### Type Hints

All public functions should include type hints:

```python
from typing import Optional, List, Dict, Any

def process_data(
    data: bytes,
    options: Optional[Dict[str, Any]] = None
) -> List[str]:
    """Process binary data and return list of strings."""
    ...
```

## 📝 Commit Guidelines

### Conventional Commits Format

```
<type>(<scope>): <subject>

<body>
<footer>
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code style changes (formatting, etc.) |
| `refactor` | Code refactoring |
| `test` | Adding/updating tests |
| `chore` | Build/process/tool changes |

### Examples

```bash
# Feature
feat(server): add connection rate limiting

# Bug fix
fix(client): resolve memory leak in reconnection logic

# Documentation
docs(readme): add Docker deployment examples

# Test
test(protocol): add edge case tests for large messages

# Refactor
refactor(security): simplify key management flow
```

## 🔀 Pull Request Process

### Before Submitting

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Docstrings updated for new/modified functions
- [ ] Tests added for new features
- [ ] All tests pass locally (`pytest tests/ -v`)
- [ ] Coverage maintained or improved
- [ ] No new warnings introduced

### PR Template

When opening a PR, use this template:

```markdown
## Description
Brief description of changes.

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Breaking change (fix or feature causing existing functionality to change)
- [ ] Documentation update

## Testing
How was this tested?

## Screenshots (if applicable)
Add screenshots for UI changes.

## Checklist
- [ ] My code follows style guidelines
- [ ] I have performed self-review
- [ ] I have commented my code
- [ ] My changes generate no warnings
- [ ] I have added tests that prove fix is effective
- [ ] New and existing unit tests pass locally
- [ ] Any dependent changes have been merged
```

### Review Process

1. At least one maintainer approval required
2. All CI checks must pass
3. Reviewer feedback should be addressed
4. Squash merge into main branch

## 🧪 Testing Guidelines

### Writing Tests

```python
# tests/test_new_feature.py
import pytest
from socketcomm import SomeClass

class TestNewFeature:
    """Tests for the new feature."""
    
    def setup_method(self):
        """Set up test fixtures."""
        self.instance = SomeClass()
    
    def test_basic_functionality(self):
        """Test basic functionality works."""
        result = self.instance.do_something()
        assert result is not None
    
    def test_edge_cases(self):
        """Test edge cases."""
        with pytest.raises(ValueError):
            self.instance.do_something_invalid()
    
    def test_integration(self):
        """Test integration with other components."""
        # Integration test here
        pass
```

### Test Coverage Goals

| Area | Minimum Coverage |
|------|------------------|
| Core modules (protocol, security) | 90%+ |
| Server/Client | 85%+ |
| Utilities | 80%+ |
| GUI | 70%+ |
| Overall | >80% |

### Running Tests

```bash
# All tests with verbose output
pytest tests/ -v

# With coverage report
pytest tests/ --cov=socketcomm --cov-report=html

# Specific module
pytest tests/test_socketcomm.py -k "test_security"

# Parallel testing (install pytest-xdist first)
pytest tests/ -n auto
```

## 📖 Documentation

### When to Update Documentation

Update documentation when you:
- Add new features or APIs
- Change existing behavior
- Fix bugs in documented features
- Add configuration options
- Modify project structure

### Documentation Files to Update

| Change Type | File(s) to Update |
|-------------|-------------------|
| New API | `docs/api/API_Reference.md` |
| New feature | `README.md`, relevant guide |
| Architecture change | `docs/architecture/System_Design.md` |
| Config option | `config/app-config.yaml`, README |
| Example | `examples/` directory |

### Documentation Style

```markdown
## Function Name

Brief description of what it does.

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `param1` | `str` | Description |
| `param2` | `int`, optional | Description (default: 0) |

### Returns

| Type | Description |
|------|-------------|
| `bool` | Return description |

### Raises

| Exception | When |
|-----------|------|
| `ValueError` | If invalid input |

### Example

```python
result = function_name("example")
print(result)  # Output: True
```
```

## ❓ Questions?

If you have questions:

- Check existing [Issues](https://github.com/YOUR_USERNAME/NetComm-Pro/issues)
- Start a [Discussion](https://github.com/YOUR_USERNAME/NetComm-Pro/discussions)
- Contact maintainers at: dev@netcomm-pro.dev

---

<div align="center">

**Thank you for contributing to NetComm-Pro!** 🎉

*Your efforts make this project better for everyone.*

</div>
