import uvicorn
from fastapi import FastAPI

from dto.request import Request
from service.regressor import get_session_duration

app = FastAPI()


@app.post('/routing/getDcChargeDuration')
async def get_duration(request_dto: Request):
    res = get_session_duration(request_dto.energy,
                               request_dto.temperature)
    return {"timeCharge": res}


if __name__ == '__main__':
    uvicorn.run(app, host='127.0.0.1', port=8000)
