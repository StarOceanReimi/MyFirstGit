(function (factory) {
    this.Testing = factory();
})(function () {

    function cp(dest, src) {
        if (!src) {
            return;
        }
        for (var key in src) {
            dest[key] = src[key];
        }
    }

    function createTestConsole(contentStyle) {
        var testConsole = document.createElement("div");
        cp(testConsole.style, {
            border: '1px solid #ccc',
            position: 'absolute',
            width: '92%',
            height: '95%',
            backgroundColor: '#000',
            overflowY: 'auto',
            wordWrap: "break-word",
            overflowX: 'hidden',
            color: '#f86',
            padding: '5px .5em 5px .5em'
        });
        testConsole.textline = function (obj, style) {
            var defaultStyle = {
                whiteSpace: "pre-wrap",
                margin: '0px'
            };
            cp(defaultStyle, contentStyle);
            cp(defaultStyle, style);
            testConsole.outText(obj, "p", defaultStyle);
        };

        testConsole.text = function (obj, style) {
            var defaultStyle = {
                whiteSpace: "pre-wrap"
            };
            cp(defaultStyle, contentStyle);
            cp(defaultStyle, style);
            testConsole.outText(obj, "span", defaultStyle);
        }

        testConsole.htmlline = function (obj, style) {
            var defaultStyle = {
                whiteSpace: "pre-wrap",
                margin: '0px'
            };
            cp(defaultStyle, contentStyle);
            cp(defaultStyle, style);
            testConsole.outHtml(obj, "p", defaultStyle);
        }
        
        testConsole.html = function (obj, style) {
            var defaultStyle = {
                whiteSpace: "pre-wrap"
            };
            cp(defaultStyle, contentStyle);
            cp(defaultStyle, style);
            testConsole.outHtml(obj, "span", defaultStyle);
        }
        
        testConsole.draw = function(drawFunc, options) {
            var defaultStyle = {
                whiteSpace: "pre-wrap",
                width : '100%'
            };
            if(!options) {
                options = {};
            }
            if(!options.style) {
                options.style = {};
            }
            cp(defaultStyle, options.style);
            options.style = defaultStyle;
            testConsole.outCanvas(drawFunc, options);
        }

        function toStringObject(obj) {
            var stringObject;
            if (obj === null || obj === undefined) {
                stringObject = "null or undefined object"
            } else if (typeof obj === 'string') {
                stringObject = obj;
            } else if (typeof obj === 'number' || typeof obj === 'boolean') {
                stringObject = (obj).toString();
            } else if (typeof obj === 'function') {
                stringObject = obj.name ? obj.name : obj.toString();
            } else if (/Array/.test(obj.constructor.toString())) {
                if (obj && obj.join) {
                    stringObject = obj.join(", ");
                } else {
                    stringObject = obj + "";
                }
            } else {
                stringObject = JSON.stringify(obj);
            }
            return stringObject;
        }
        
        function maxElements(c) {
            if(!c.maxElements) {
                return;
            }
            var maxElements;
            try {
                maxElements = parseInt(c.maxElements);
            } catch (ex) {
                return;
            }
            
            while(c.childNodes.length > maxElements) {
                c.removeChild(c.firstChild);
            };
        }

        testConsole.outHtml = function (obj, elementType, style) {
            var output = document.createElement(elementType);
            cp(output.style, style);
            var stringObject = toStringObject(obj);
            output.innerHTML = stringObject;
            this.appendChild(output);
            maxElements(testConsole);
        }

        testConsole.outText = function (obj, elementType, style) {
            var output = document.createElement(elementType);
            cp(output.style, style);
            var stringObject = toStringObject(obj);
            var textNode = document.createTextNode(stringObject);
            output.appendChild(textNode);
            this.appendChild(output);
            maxElements(testConsole);
        };
        
        testConsole.outCanvas = function(drawFunc, options) {
            var canvas = document.createElement('canvas');
            cp(canvas, options);
            drawFunc(canvas);
            this.appendChild(canvas);
        }
        return testConsole;
    }
    
    function Testing() {
        var testConsole;
        this.showConsole = function (options) {
            var children = document.body.childNodes;
            var hasConsole = false;
            for (var i = 0; i < children.length; i++) {
                if (testConsole === children[i]) {
                    hasConsole = true;
                    break;
                }
            }
            options = options || {};
            if (!hasConsole) {
                testConsole = createTestConsole(options.contentStyle);    
                document.body.appendChild(testConsole);
            } else {
                testConsole = createTestConsole(options.contentStyle);
            }
            cp(testConsole, options);
            cp(testConsole.style, (options.consoleStyle || {}));
            testConsole.style.display = 'block';
            this.console = function() { return testConsole; };
        };
        this.run = function (suites) {
            function printlnSuite(suite) {
                testConsole.textline("===========BEGIN===========", {
                    marginBottom: "-3px"
                });
                testConsole.textline(suite.name, {
                    fontWeight: 'bold',
                    padding : '0 .5em 0 .5em'
                });
                testConsole.textline("---------------------------", {
                    marginTop: "-3px"
                });
                suite.run(testConsole);
                testConsole.textline("============END============");
                testConsole.textline(" ");
            }
            var ctorString = suites.constructor.toString();
            if (/Array/.test(ctorString)) {
                suites.forEach(function (t) {
                    printlnSuite(t);
                });
            } else if (typeof suites === 'object') {
                printlnSuite(suites);
            }
        };
    }
    return new Testing();
});
