from fastapi import FastAPI

from dto.request import Request
from service.regressor import get_session_duration
import pickle

app = FastAPI()

scaler = pickle.load(open("model/scaler.pkl", "rb"))
model = pickle.load(open("model/ann.pkl", "rb"))


@app.post('/routing/getDcChargeDuration')
async def get_duration(request_dto: Request):
    global scaler
    global model
    res = get_session_duration(request_dto.temperature,
                               request_dto.energy,
                               scaler,
                               model)

    return {"timeCharge": res}
