import pickle

import pandas as pd


def get_session_duration(energy, temperature):
    poly_model = pickle.load(open("model/poly_model.pkl", "rb"))
    regression_model = pickle.load(open("model/regression_model.pkl", "rb"))

    df = pd.DataFrame(data={"energy": energy,
                            "temperature": temperature})
    x = df[df.columns.to_list()].values
    poly_x = poly_model.fit_transform(x)

    return round(regression_model.predict(poly_x)[0], 3)
