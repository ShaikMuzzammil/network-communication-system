#!/usr/bin/env python3
"""
SocketCommunication Package Setup
=================================

A complete, production-ready socket communication system.
"""

from setuptools import setup, find_packages
from pathlib import Path

# Read README for long description
readme_path = Path(__file__).parent / "README.md"
long_description = readme_path.read_text(encoding="utf-8") if readme_path.exists else ""

# Read version from package
version = "2.0.0"

setup(
    name="socketcommunication",
    version=version,
    author="SocketCommunication Team",
    author_email="dev@socketcomm.dev",
    description="A complete, production-ready socket communication system",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/socketcomm/socketcommunication",
    license="MIT",
    
    # Package discovery
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    
    # Python version requirement
    python_requires=">=3.8",
    
    # Dependencies
    install_requires=[
        "pycryptodome>=3.19.0",
        "PyYAML>=6.0",
    ],
    
    # Optional dependencies
    extras_require={
        "dev": [
            "pytest>=7.4.0",
            "pytest-cov>=4.1.0",
            "black>=23.0.0",
            "flake8>=6.1.0",
            "mypy>=1.5.0",
        ],
        "docs": [
            "sphinx>=7.1.0",
            "sphinx-rtd-theme>=1.3.0",
        ],
        "gui": [],  # tkinter is built-in
        "perf": [
            "uvloop>=0.17.0",
        ],
    },
    
    # Entry points
    entry_points={
        "console_scripts": [
            "socketcomm-server=socketcomm.server:main",
            "socketcomm-client=socketcomm.client:main",
            "socketcomm-gui=socketcomm.gui_client:main",
        ],
    },
    
    # Classification
    classifiers=[
        "Development Status :: 5 - Production/Stable",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
        "Topic :: Internet :: Socket Communication",
        "Topic :: Security :: Cryptography",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "Typing :: Typed",
    ],
    
    # Keywords
    keywords=[
        "socket", "communication", "networking", "tcp", "server", "client",
        "encryption", "aes", "rsa", "file-transfer", "chat", "messaging",
        "protocol", "scmp", "secure", "gui", "python", "java",
    ],
    
    # Project URLs
    project_urls={
        "Bug Tracker": "https://github.com/socketcomm/socketcommunication/issues",
        "Documentation": "https://docs.socketcomm.dev",
        "Source Code": "https://github.com/socketcomm/socketcommunication",
    },
    
    # Include data files
    include_package_data=True,
    zip_safe=False,
)
