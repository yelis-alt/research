"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var JSGantt = require("../index");
var chai_1 = require("chai");
var protractor_1 = require("protractor");
var dv = protractor_1.browser.driver;
describe('Browser test', function () {
    it('JSGantt exists', function () {
        chai_1.expect(JSGantt).to.exist;
    });
    it('Driver exists', function () {
        chai_1.expect(dv).to.exist;
    });
});
//# sourceMappingURL=index.js.map