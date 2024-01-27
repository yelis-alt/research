# библиотеки
from flask import Flask, request
from flask_cors import CORS
import openrouteservice
import math
import random
import string

# веб-приложение
app = Flask(__name__)
CORS(app, support_credentials=True)


@app.route("/", methods=['POST', 'GET'])
def task():
    resp = {}
    # приём json
    if request.method == 'POST':
        resp = request.get_json()
    params = {'start': [resp['start'].split(',')[0],
                        resp['start'].split(',')[1]],
              'finish': [resp['finish'].split(',')[0],
                         resp['finish'].split(',')[1]],
              'grad': float(resp['grad']),
              'acc': float(resp['acc']),
              'maxacc': float(resp['maxacc']),
              'spend': float(resp['spend'])}
    jsc = resp['stationsList']

    # алгоритм Дейкстры
    class Dijkstra:
        def __init__(self, jsc, params):
            self.stations = jsc
            self.params = params
            self.api = '5b3ce359785111' \
                       '0001cf6248eb51' \
                       'a5f80f97435cbf' \
                       'a27a3f642d9c19'
            self.id_start = '0'
            self.id_finish = ''

        # получение матрицы смежности из json-документа
        def matrix(self):
            # фильтрация рабочих ЭЗС
            nodes = [obj for obj in self.stations
                     if obj['plug_type'] != None]
            # объявление параметров
            spend_opt = self.params['spend']
            grad = self.params['grad']
            acc = self.params['acc']
            maxacc = self.params['maxacc']
            spend = round((spend_opt * 0.00056 * (grad ** 2 - \
                                                  40 * grad + 2200)) / 100, 3)
            r = 15
            v = 45

            # преобразование координат
            def coordinates(nodes, id):
                for ind_obj, obj in enumerate(nodes):
                    if obj['id'] == id:
                        coord_ind = ind_obj;
                return [(float(nodes[coord_ind]['lon']),
                         float(nodes[coord_ind]['lat'])),
                        coord_ind]

            # нахождение расстояния маршрута между вершинами графа
            def route(api, coord1, coord2):
                client = openrouteservice.Client(key=api)
                geo = client.directions((coord1, coord2),
                                        instructions=False,
                                        geometry=False)
                km = geo['routes'][0]['summary']['distance'] / 1000
                ch = geo['routes'][0]['summary']['duration'] / 3600
                return [round(km, 3), round(ch, 3)]

            # нахождение веса вершины графа
            def cost(id, id_start, grad, spend, r,
                     dist, v, t_dist, acc, maxacc,
                     r_station):
                if id == id_start:
                    acc_lvl = acc
                else:
                    acc_lvl = maxacc
                if grad >= 20.0:
                    weight = spend * r * (dist + v * t_dist) + \
                             spend * r * v * (float(random.randint(0, 5) / 60) + \
                                              0.76 * math.log(100 * (1 - ((acc_lvl - spend * dist) / maxacc)))) + \
                             r_station * (maxacc - (acc_lvl - spend * dist))
                else:
                    weight = spend * r * (dist + v * t_dist) + \
                             spend * r * v * (float(random.randint(0, 5) / 60) + \
                                              0.76 * math.log(abs((1100 * ((acc_lvl - \
                                                                            spend * dist) / maxacc) - 17 * grad + 750) / (
                                                                              17 * grad + 339)))) + \
                             r_station * (maxacc - (acc_lvl - spend * dist))
                return round(weight, 2)

            # формирование списка вершин графа
            coord_start = [float(params['start'][0]),
                           float(params['start'][1])]
            coord_finish = [float(params['finish'][0]),
                            float(params['finish'][1])]
            diameter = math.dist(coord_start, coord_finish) * 1.2
            middle = [sum(x) / 2 for x in zip(*[coord_start, coord_finish])]
            nodes_arr = []
            for node in nodes:
                stat_coord = [float(node['lat']),
                              float(node['lon'])]
                stat_dist = math.dist(middle, stat_coord)
                if stat_dist <= diameter / 2:
                    nodes_arr.append(node['id'])
            nodes_arr.append(self.id_start)
            nodes_arr.sort(key=lambda x: int(x))
            self.id_finish = str(int(nodes_arr[-1]) + 1)
            nodes_arr.append(self.id_finish)
            # добавление старта и финиша к графу
            add = {self.id_start: 'start',
                   self.id_finish: 'finish'}
            for attr in list(add.keys()):
                nodes.append({'id': attr,
                              'lon': params[add[attr]][1],
                              'lat': params[add[attr]][0],
                              'price': '0.0'})
            nodes.sort(key=lambda x: int(x['id']))
            # формирование смежной матрицы весов
            table = {}
            for ind_id, id in enumerate(nodes_arr):
                if id != self.id_finish:
                    table[id] = {}
                    coord_1 = coordinates(nodes, id)
                    coord1 = coord_1[0]
                    for ind_rest, rest in enumerate(nodes_arr):
                        if (id != rest) & (rest != self.id_start):
                            # расчёт расстояния
                            # расчёт продолжительности поездки
                            # расчёт стоимости 1кВт-ч на слоте ЭЗС
                            coord_2 = coordinates(nodes, rest)
                            coord2 = coord_2[0]
                            dist = route(self.api, coord1, coord2)[0]
                            t_dist = route(self.api, coord1, coord2)[1]
                            r_station = float(nodes[coord_2[1]]['price'])
                            ves = 0
                            # соединение физически достижимых вершин
                            # назначение вершинам весов
                            if (id == self.id_start) & (acc >= spend * dist):
                                ves = cost(id, self.id_start, grad, spend, r,
                                           dist, v, t_dist, acc, maxacc,
                                           r_station)
                            elif (id != self.id_start) & (maxacc >= spend * dist):
                                ves = cost(id, self.id_start, grad, spend, r,
                                           dist, v, t_dist, acc, maxacc,
                                           r_station)
                            if ves > 0:
                                table[id][rest] = ves
                            else:
                                pass
            return table

        # реализация алгоритма Дейкстры
        def solve(self, parameter):
            try:
                # объявление переменных
                graph = self.matrix()
                print(graph)
                start = self.id_start
                finish = self.id_finish
                route = {}
                connect_node = {}
                queue = []
                res = {}
                graph[finish] = {}
                # выполнение алгоритма Дейкстры
                for node in graph:
                    route[node] = float("inf")
                    connect_node[node] = None
                    queue.append(node)
                # перебор вершин формируемого пути
                route[start] = 0
                while queue:
                    key_min = queue[0]
                    val_min = route[key_min]
                    for n in range(1, len(queue)):
                        if route[queue[n]] < val_min:
                            key_min = queue[n]
                            val_min = route[key_min]
                    now = key_min
                    queue.remove(now)
                    # включение вершины в оптимальный путь
                    for i in graph[now]:
                        other = graph[now][i] + route[now]
                        try:
                            if route[i] > other:
                                route[i] = other
                                connect_node[i] = now
                        except:
                            pass
                # формирование результата алгоритма
                res['path'] = []
                res['path'].append(finish)
                while True:
                    finish = connect_node[finish]
                    if finish is None:
                        break
                    res['path'].append(finish)
                res['path'].reverse()
                # расчёт стоимости оптимального пути
                suma = 0
                opt = res['path']
                for index, node in enumerate(opt):
                    if index != len(opt) - 1:
                        suma += graph[node][opt[index + 1]]
                res['cost'] = suma
                # формирование массива оптимального пути
                res['path'][0] = '|-->'
                res['path'][-1] = '-->|'
                if res['path'] == ['-->|']:
                    error = 5 / 0
                return res[parameter]
            except:
                res = {'path': 'impossible',
                       'cost': 'impossible'}
                return res[parameter]

    # генетические алгоритмы
    class Genetic(Dijkstra):
        def __init__(self, jsc, params):
            super().__init__(jsc, params)

        def matrix(self):
            return super().matrix()

        def solve(self, parameter):
            try:
                graph = self.matrix()

                # список ключей:
                def key(dictionary):
                    return list(dictionary.keys())

                # формирование пустой хромосомы
                parents = []
                dna = {}
                for node in key(graph):
                    dna[node] = 0
                dna[self.id_start] = 1
                dna[self.id_finish] = 1

                # оператор мутации
                def mutation(dna):
                    n = len(key(dna))
                    ind_gene = random.randint(1, n - 2)
                    gene = key(dna)[ind_gene]
                    dna[gene] = 1 - dna[gene]
                    parents.append(dna)
                    return {'dna': dna,
                            'parents': parents}

                # оператор кроссовера
                def crossover(dna):
                    if len(parents) < 3:
                        return mutation(dna)
                    else:
                        n = len(parents)
                        ind_parent1 = random.randint(0, n - 1)
                        ind_parent2 = random.randint(0, n - 1)
                        parent1 = parents[ind_parent1]
                        parent2 = parents[ind_parent2]
                        genes = key(dna)
                        new_dna = {}
                        for gene in genes:
                            if (gene != self.id_start) & \
                                    (gene != self.id_finish):
                                new_dna[gene] = parent1[gene] + \
                                                parent2[gene]
                            else:
                                new_dna[gene] = 1
                            if new_dna[gene] == 2:
                                new_dna[gene] == random.randint(0, 1)
                        parents.append(new_dna)
                        return {'dna': new_dna,
                                'parents': parents}

                # выполнение процесса эволюции
                best_fit = float('inf')
                rep = -1
                success = 0
                success_rate = 10
                rep_rate = 100000
                while success < success_rate:
                    rep += 1
                    # выход из цикла
                    if rep == rep_rate:
                        if success == 0:
                            res = {'path': 'impossible',
                                   'cost': 'impossible'}
                        success = success_rate
                    fit = 0
                    fail = 0
                    rand = random.randint(0, 3)
                    if rand == 0:
                        dna = mutation(dna)['dna']
                    elif rand == 1:
                        dna = crossover(dna)['dna']
                    elif rand == 2:
                        dna = mutation(dna)['dna']
                        dna = crossover(dna)['dna']
                    else:
                        dna = crossover(dna)['dna']
                        dna = mutation(dna)['dna']
                    # проверка приспособленности хромосомы
                    path = [gene for gene in key(dna) \
                            if dna[gene] == 1]
                    for ind_node, node in enumerate(path):
                        if ind_node != len(path) - 1:
                            next_node = path[ind_node + 1]
                            if next_node in key(graph[node]):
                                fit += graph[node][next_node]
                            else:
                                fail += 1
                                break
                    if fail == 0:
                        if fit < best_fit:
                            best_fit = fit
                            best_dna = path
                            res = {'path': best_dna,
                                   'cost': best_fit}
                            success += 1
                # формирование массива пути
                if res['path'] != 'impossible':
                    res['path'][0] = '|-->'
                    res['path'][-1] = '-->|'
                return res[parameter]
            except:
                res = {'path': 'impossible',
                       'cost': 'impossible'}
                return res[parameter]

    # решение задачи
    tasks = Dijkstra(jsc, params)
    login = ''.join(random.choice(string.ascii_uppercase +
                                  string.digits) for _ in range(8))
    solution = {login: tasks.solve('path')}

    # сравнение методов
    """
    import datetime
    def curr_time():
        date = datetime.datetime.now()
        return date
    def attempt(tool, test, step, itera):
        cum_sum, cum_dur = 0, 0
        n_exp = itera
        for n in range(itera):
            if tool == 'Алгоритм Дейкстры':
                approach = Dijkstra(stationsList, params)
            else:
                approach = Genetic(stationsList, params)
            now = curr_time()
            price = approach.solve(test,'cost')
            cum_dur += (curr_time() - now).total_seconds()
            if price != 'impossible':
                cum_sum += price
            else:
                n_exp -= 1
        test_res[tool]['Условная стоимость']\
                [str(step)]= round(cum_sum/n_exp, 2)
        test_res[tool]['Длительность']\
                [str(step)]= round(cum_dur/n_exp, 6)
    #чтение тестовой матрицы
    doc = 'test_matrix.json'
    with open(doc, encoding='utf-8', mode='r') as file:
        test = json.load(file)
    #тестирование
    id_last = list(test.keys())[-1]
    test_arr = list(test.keys())[:-5]
    test_arr.append(id_last)
    test_res = {'Алгоритм Дейкстры':
                    {'Условная стоимость': {},
                     'Длительность': {}},
                'Генетические алгоритмы':
                    {'Условная стоимость': {},
                     'Длительность': {}}}
    for exp in range(5):
        step = len(test_arr) - 5*exp
        itera = 10
        if step != 0:
            sample = test_arr[:step-1]
            sample.append(id_last)
            test = {key: test[key] for key in sample}
            attempt('Алгоритм Дейкстры',
                    test, step, itera)
            attempt('Генетические алгоритмы',
                    test, step, itera)
    test_res
    """

    print(solution)
    return solution


if __name__ == '__main__':
    app.run(debug=True)
