#!/usr/bin/env python3
"""
db_prod_to_mysql.py - Temporary helper to move deployed SQLite data into MySQL.

This script does not change the cockpit deployment. It performs:
1) db_prod2local.py with SQLite mode forced so the deployed SQLite data is pulled into local volumes/sqlite.db
2) db_init_local_to_mysql.py to push that local SQLite snapshot into MySQL

Use this while the deployed app still points at SQLite.
"""

from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent


def run_script(script_name: str, extra_env: dict[str, str] | None = None) -> int:
    env = os.environ.copy()
    if extra_env:
        env.update(extra_env)

    result = subprocess.run([sys.executable, f"scripts/{script_name}"], cwd=PROJECT_ROOT, env=env)
    return result.returncode


def main() -> int:
    print("============================================================")
    print("TEMPORARY PROD SQLITE -> MYSQL MIGRATION")
    print("============================================================")

    print("\n[1/2] Pulling deployed SQLite data into local SQLite...")
    sqlite_env = {
        "DB_URL": "jdbc:sqlite:volumes/sqlite.db?journal_mode=WAL",
        "DB_DRIVER": "org.sqlite.JDBC",
        "DB_DIALECT": "org.hibernate.community.dialect.SQLiteDialect",
        "DB_USERNAME": "admin",
        "DB_PASSWORD": "admin",
    }
    code = run_script("db_prod2local.py", extra_env=sqlite_env)
    if code != 0:
        print("\nStep 1 failed.")
        return code

    print("\n[2/2] Pushing local SQLite snapshot into MySQL...")
    code = subprocess.run(
        [
            sys.executable,
            "scripts/mysqlrestore.py",
            "--backup-file",
            "volumes/sqlite.db",
            "--force",
        ],
        cwd=PROJECT_ROOT,
    ).returncode
    if code != 0:
        print("\nStep 2 failed.")
        return code

    print("\n============================================================")
    print("PROD SQLITE -> MYSQL MIGRATION COMPLETE")
    print("============================================================")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
