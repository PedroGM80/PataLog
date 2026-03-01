"""
Protocolo de comunicacion JSON sobre stdin/stdout.

Este modulo encapsula la lectura y escritura de mensajes,
permitiendo que el resto del backend no se preocupe por el formato.
"""

import sys
import json
from typing import Any
from dataclasses import dataclass


@dataclass
class Request:
    """Mensaje entrante desde Kotlin."""
    id: str
    action: str
    params: dict[str, Any]


@dataclass  
class Response:
    """Mensaje de respuesta hacia Kotlin."""
    id: str
    ok: bool
    data: dict[str, Any] | None = None
    error: str | None = None

    def to_json(self) -> str:
        obj = {"id": self.id, "ok": self.ok}
        if self.data:
            obj.update(self.data)
        if self.error:
            obj["error"] = self.error
        return json.dumps(obj, ensure_ascii=False)


def log(msg: str) -> None:
    """Log a stderr (no interfiere con el protocolo JSON)."""
    print(f"[backend] {msg}", file=sys.stderr, flush=True)


def read_request() -> Request | None:
    """
    Lee una linea de stdin y la parsea como Request.
    Retorna None si stdin se cierra.
    """
    try:
        line = sys.stdin.readline()
        if not line:
            return None
        line = line.strip()
        if not line:
            return None
        
        data = json.loads(line)
        return Request(
            id=data.get("id", ""),
            action=data.get("action", ""),
            params={k: v for k, v in data.items() if k not in ("id", "action")}
        )
    except json.JSONDecodeError as e:
        # Enviamos error y seguimos esperando
        send_response(Response(id="", ok=False, error=f"JSON invalido: {e}"))
        return Request(id="", action="", params={})


def send_response(response: Response) -> None:
    """Envia una respuesta JSON por stdout."""
    print(response.to_json(), flush=True)


def send_ok(request_id: str, **data: Any) -> None:
    """Atajo para enviar respuesta exitosa."""
    send_response(Response(id=request_id, ok=True, data=data if data else None))


def send_error(request_id: str, error: str) -> None:
    """Atajo para enviar respuesta de error."""
    send_response(Response(id=request_id, ok=False, error=error))
