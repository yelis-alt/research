from typing import List

from pydantic import BaseModel


class Request(BaseModel):
    energy: List[float]
    temperature: List[float]
