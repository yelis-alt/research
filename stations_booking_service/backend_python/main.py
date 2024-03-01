import uvicorn
from fastapi import FastAPI

from dto.request import Request
from service.regressor import get_session_duration

app = FastAPI()


@app.post('/routing/getDcChargeDuration')
async def get_duration(request_dto: Request):
    res = get_session_duration(request_dto.temperature,
                               request_dto.energy)
    return {"timeCharge": res}
