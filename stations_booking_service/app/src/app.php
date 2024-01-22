<?php
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing;

function render_template($request){
    extract($request->attributes->all(), EXTR_SKIP);
    ob_start();
    include sprintf(__DIR__.'/templates/main.php', $_route);

    return new Response(ob_get_clean());
}

function getWeatherDataXml($cache_life, $city) {
$weather = array();
    $cache_file = $_SERVER['DOCUMENT_ROOT']."/backend/weather.txt";
    $url='http://export.yandex.ru/bar/reginfo.xml?region='.$city.'.xml';
    if (time() - @filemtime($cache_file) >= $cache_life) {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $data = curl_exec($ch);
        curl_close($ch);
        file_put_contents($cache_file, $data);
        $buf = file_get_contents($url);
        if ($buf) file_put_contents($cache_file, $buf);
    }
    $xml = simplexml_load_file($cache_file);
    $weather['temp'] = $xml->weather->day->day_part[0]->temperature;
    if (substr($weather['temp'], 0, 1) == '+') {
        $weather['temp'] = ltrim($weather['temp'], '+');
    };
    return $weather['temp'];
}

function quer($con, $plug, $ac_dc, $from_power, $to_power,
              $from_price, $to_price){
    $statement = $con->prepare('SELECT * FROM Stations 
                                WHERE (plug='.'"'.(string)$plug.'" OR plug IS NULL) AND
                                      (plug_type IN '.(string)$ac_dc.' OR plug_type IS NULL) AND
                                      ((power>='.(string)$from_power.' AND power<='.(string)$to_power.') OR power IS NULL) AND
                                      ((price>='.(string)$from_price.' AND price<='.(string)$to_price.') OR price IS NULL)');
    $statement->execute();
    $results = $statement->fetchAll(PDO::FETCH_ASSOC);
    $json = json_encode($results, JSON_UNESCAPED_UNICODE);
    return $json;
}

function overwrite($resp){
    $path = $_SERVER['DOCUMENT_ROOT']."/schedule.json";
    file_put_contents($path, json_encode($resp));
}

$routes = new Routing\RouteCollection();
$routes->add('main', new Routing\Route('/chargebook', [
    '_controller' => 'render_template'
]));

$routes->add('weather', new Routing\Route('/weather', [
    '_controller' => function ($request) {
        $request = json_decode($request->getContent(), true);
        $weather = getWeatherDataXml(3600, (string)$request['city']);
        $path = $_SERVER['DOCUMENT_ROOT']."/backend/weather.txt";
        unlink($path);
        return new Response($weather);
    }
]));
$routes->add('stations', new Routing\Route('/stations', [
    '_controller' => function ($request) {
        $request = json_decode($request->getContent(), true);
        $pdo = new PDO('mysql:host=mysql;
                         dbname=ELECTRO; 
                         charset=UTF8',
            'arsel',
            'minidelphi');
        $jsc = quer($pdo, $request['plug'], $request['acDc'],
                    $request['fromPower'], $request['toPower'],
                    $request['fromPrice'], $request['toPrice']);
        return new Response($jsc);
    }
]));

$routes->add('schedule', new Routing\Route('/schedule', [
    '_controller' => function ($request) {
        $request = json_decode($request->getContent(), true);
        $resp = $request['resp'];
        overwrite($resp);
        $success = array('result' => 'success');
        $res = json_encode($success, True);
        return new Response($res);
    }
]));

return $routes;