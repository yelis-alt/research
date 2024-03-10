import pickle

import pandas as pd


def get_session_duration(temp, accepted_energy, scaler, model):
    x = pd.DataFrame(data={"temp": temp,
                           "accepted_energy": accepted_energy})
    x_scaled = scaler.transform(x)

    return round(float(model.predict(x_scaled)[0][0]), 3)
