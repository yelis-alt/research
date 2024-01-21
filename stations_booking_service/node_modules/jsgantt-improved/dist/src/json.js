"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.addJSONTask = exports.parseJSONString = exports.parseJSON = void 0;
var task_1 = require("./task");
var general_utils_1 = require("./utils/general_utils");
/**
 *
 * @param pFile
 * @param pGanttlet
 */
exports.parseJSON = function (pFile, pGanttVar, vDebug, redrawAfter) {
    if (vDebug === void 0) { vDebug = false; }
    if (redrawAfter === void 0) { redrawAfter = true; }
    return __awaiter(this, void 0, void 0, function () {
        var jsonObj, bd, ad;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, general_utils_1.makeRequest(pFile, true, true)];
                case 1:
                    jsonObj = _a.sent();
                    if (vDebug) {
                        bd = new Date();
                        console.info('before jsonparse', bd);
                    }
                    exports.addJSONTask(pGanttVar, jsonObj);
                    if (this.vDebug) {
                        ad = new Date();
                        console.info('after addJSONTask', ad, (ad.getTime() - bd.getTime()));
                    }
                    if (redrawAfter) {
                        pGanttVar.Draw();
                    }
                    return [2 /*return*/, jsonObj];
            }
        });
    });
};
exports.parseJSONString = function (pStr, pGanttVar) {
    exports.addJSONTask(pGanttVar, JSON.parse(pStr));
};
exports.addJSONTask = function (pGanttVar, pJsonObj) {
    for (var index = 0; index < pJsonObj.length; index++) {
        var id = void 0;
        var name_1 = void 0;
        var start = void 0;
        var end = void 0;
        var planstart = void 0;
        var planend = void 0;
        var itemClass = void 0;
        var planClass = void 0;
        var link = '';
        var milestone = 0;
        var resourceName = '';
        var completion = void 0;
        var group = 0;
        var parent_1 = void 0;
        var open_1 = void 0;
        var dependsOn = '';
        var caption = '';
        var notes = '';
        var cost = void 0;
        var duration = '';
        var bartext = '';
        var additionalObject = {};
        for (var prop in pJsonObj[index]) {
            var property = prop;
            var value = pJsonObj[index][property];
            switch (property.toLowerCase()) {
                case 'pid':
                case 'id':
                    id = value;
                    break;
                case 'pname':
                case 'name':
                    name_1 = value;
                    break;
                case 'pstart':
                case 'start':
                    start = value;
                    break;
                case 'pend':
                case 'end':
                    end = value;
                    break;
                case 'pplanstart':
                case 'planstart':
                    planstart = value;
                    break;
                case 'pplanend':
                case 'planend':
                    planend = value;
                    break;
                case 'pclass':
                case 'class':
                    itemClass = value;
                    break;
                case 'pplanclass':
                case 'planclass':
                    planClass = value;
                    break;
                case 'plink':
                case 'link':
                    link = value;
                    break;
                case 'pmile':
                case 'mile':
                    milestone = value;
                    break;
                case 'pres':
                case 'res':
                    resourceName = value;
                    break;
                case 'pcomp':
                case 'comp':
                    completion = value;
                    break;
                case 'pgroup':
                case 'group':
                    group = value;
                    break;
                case 'pparent':
                case 'parent':
                    parent_1 = value;
                    break;
                case 'popen':
                case 'open':
                    open_1 = value;
                    break;
                case 'pdepend':
                case 'depend':
                    dependsOn = value;
                    break;
                case 'pcaption':
                case 'caption':
                    caption = value;
                    break;
                case 'pnotes':
                case 'notes':
                    notes = value;
                    break;
                case 'pcost':
                case 'cost':
                    cost = value;
                    break;
                case 'duration':
                case 'pduration':
                    duration = value;
                    break;
                case 'bartext':
                case 'pbartext':
                    bartext = value;
                    break;
                default:
                    additionalObject[property.toLowerCase()] = value;
            }
        }
        //if (id != undefined && !isNaN(parseInt(id)) && isFinite(id) && name && start && end && itemClass && completion != undefined && !isNaN(parseFloat(completion)) && isFinite(completion) && !isNaN(parseInt(parent)) && isFinite(parent)) {
        pGanttVar.AddTaskItem(new task_1.TaskItem(id, name_1, start, end, itemClass, link, milestone, resourceName, completion, group, parent_1, open_1, dependsOn, caption, notes, pGanttVar, cost, planstart, planend, duration, bartext, additionalObject, planClass));
        //}
    }
};
//# sourceMappingURL=json.js.map