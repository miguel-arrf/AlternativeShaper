Blockly.defineBlocksWithJsonArray([

  //CHANGE_HERE

  {
    "type": "existingproc0",
    "message0": "proc_0: %1",
    "args0": [
      {
        "type": "field_input",
        "name": "proc0",
        "text": "insert existing proc0 name"
      }
    ],
    "output": "proc0",
    "colour": 230,
    "tooltip": "",
    "helpUrl": ""
  },

  {
    "type": "existingproc1",
    "message0": "proc1: %1",
    "args0": [
      {
        "type": "field_input",
        "name": "proc1",
        "text": "insert existing proc1 name"
      }
    ],
    "output": "proc1",
    "colour": 230,
    "tooltip": "",
    "helpUrl": ""
  },

  {
    "type": "existingbool",
    "message0": "bool: %1",
    "args0": [
      {
        "type": "field_input",
        "name": "bool",
        "text": "insert existing bool name"
      }
    ],
    "output": "bool",
    "colour": 230,
    "tooltip": "",
    "helpUrl": ""
  },

  {
    "type": "existingproc",
    "message0": "proc: %1",
    "args0": [
      {
        "type": "field_input",
        "name": "proc",
        "text": "insert existing proc name"
      }
    ],
    "output": "proc",
    "colour": 230,
    "tooltip": "",
    "helpUrl": ""
  },

  {
  "type": "proc",
  "message0": "proc: %1 %2",
  "args0": [
    {
      "type": "field_input",
      "name": "NAME",
      "text": "name"
    },
    {
      "type": "input_value",
      "name": "NAME",
      "check": "proc"
    }
  ],
  "inputsInline": true,
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},



{
  "type": "pickone",
  "message0": "pickone %1 proc %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "pickfirst",
  "message0": "pickFirst %1 proc %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "verify",
  "message0": "verify %1 bool %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_statement",
      "name": "bool",
      "check": "bool"
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "input",
  "message0": "input %1 string %2 var %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "string",
      "check": "string"
    },
    {
      "type": "input_value",
      "name": "var",
      "check": "var"
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "while",
  "message0": "while %1 bool %2 proc %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "proc",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "if",
  "message0": "if %1 bool %2 proc %3 proc %4",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "proc_1",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    },
    {
      "type": "input_value",
      "name": "proc_2",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "and",
  "message0": "and %1 proc %2 proc %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    },
    {
      "type": "input_value",
      "name": "proc_1",
      "check":  [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "bool",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "or",
  "message0": "or %1 proc %2 proc %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc",
      "check": [
        "proc",
        "proc0",
        "proc1"
      ]
    },
    {
      "type": "input_value",
      "name": "proc_1",
      "check": [
        "proc",
        "proc0",
        "proc1"
      ]
    }
  ],
  "output": "bool",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "nothing",
  "message0": "nothing",
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "global",
  "message0": "global %1 proc0 %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc0",
      "check": "proc0"
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "initialshape",
  "message0": "initialshape %1 shape %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    }
  ],
  "output": "proc",
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "assign",
  "message0": "assign %1 var %2 alltype %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "var",
      "check": "var"
    },
    {
      "type": "input_value",
      "name": "alltype",
      "check": "alltype"
    }
  ],
  "output": "proc1",
  "colour": 80,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "seq",
  "message0": "seq %1 proc_1 %2 proc_2 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "proc_1",
      "check": "proc"
    },
    {
      "type": "input_value",
      "name": "proc_2",
      "check": "proc"
    }
  ],
  "output": "proc1",
  "colour": 80,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "src_1",
  "message0": "src_1 %1 bool %2 shape %3 shape %4 proc1 %5",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "proc1",
      "check": "proc1"
    }
  ],
  "output": "proc0",
  "colour": 20,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "src",
  "message0": "src %1 bool %2 shape %3 shape %4",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "NAME",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "proc0",
  "colour": 20,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "sr_1",
  "message0": "sr_1 %1 shape %2 shape %3 proc1 %4",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "proc1",
      "check": "proc1"
    }
  ],
  "output": "proc0",
  "colour": 20,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "sr",
  "message0": "sr %1 shape %2 shape %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "proc0",
  "colour": 20,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "id",
  "message0": "id %1 %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "field_input",
      "name": "id",
      "text": "id"
    }
  ],
  "output": "proc0",
  "colour": 20,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "opposite",
  "message0": "opposite %1 shape %2 nat %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "nat",
      "check": "nat"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "oncorner",
  "message0": "oncorner: %1 shape %2 corner: %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "field_dropdown",
      "name": "corner",
      "options": [
        [
          "frontright",
          "frontright"
        ],
        [
          "frontleft",
          "frontleft"
        ],
        [
          "backright",
          "backright"
        ],
        [
          "backleft",
          "backleft"
        ]
      ]
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "onborder",
  "message0": "onborder: %1 shape %2 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "field_dropdown",
      "name": "direction",
      "options": [
        [
          "right",
          "right"
        ],
        [
          "left",
          "left"
        ],
        [
          "front",
          "front"
        ],
        [
          "back",
          "back"
        ]
      ]
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "next",
  "message0": "next %1 shape %2 shape_1 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "overlapped",
  "message0": "overlapped %1 shape %2 shape_1 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "vmidle",
  "message0": "vmidle %1 shape %2 shape_1 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "hmidle",
  "message0": "hmidle %1 shape %2 shape_1 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "shape_1",
      "check": "shape"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "equals",
  "message0": "equals %1 alltype %2 alltype_1 %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "alltype"
    },
    {
      "type": "input_value",
      "name": "alltype_1"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "all",
  "message0": "all %1 shape %2 bool %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "some",
  "message0": "some %1 shape %2 bool %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "shape",
      "check": "shape"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "not",
  "message0": "not %1 bool %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "or_bool",
  "message0": "or_bool %1 bool %2 bool %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "bool_1",
      "check": "bool"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "and_bool",
  "message0": "and_bool %1 bool %2 bool %3",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "bool",
      "check": "bool"
    },
    {
      "type": "input_value",
      "name": "bool_1",
      "check": "bool"
    }
  ],
  "output": "bool",
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "shape",
  "message0": "%1",
  "args0": [
    {
      "type": "field_input",
      "name": "shape",
      "text": "insert shape name"
    }
  ],
  "output": "shape",
  "colour": 180,
  "tooltip": "",
  "helpUrl": ""
}
]);

Blockly.JavaScript['proc'] = function(block) {
  var text_name = block.getFieldValue('NAME');
  var value_name = Blockly.JavaScript.valueToCode(block, 'NAME', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "proc(" + text_name + ',' + value_name + ')';
  return code;
};

Blockly.JavaScript['pickone'] = function(block) {
  var value_proc = Blockly.JavaScript.valueToCode(block, 'proc', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "pickone(" + value_proc + ")"
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['pickfirst'] = function(block) {
  var value_proc = Blockly.JavaScript.valueToCode(block, 'proc', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'pickfirst(' + value_proc + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['verify'] = function(block) {
  var statements_bool = Blockly.JavaScript.statementToCode(block, 'bool');
  // TODO: Assemble JavaScript into code variable.
  var code = 'verify(' + statements_bool + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['input'] = function(block) {
  var value_string = Blockly.JavaScript.valueToCode(block, 'string', Blockly.JavaScript.ORDER_ATOMIC);
  var value_var = Blockly.JavaScript.valueToCode(block, 'var', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'input(' + value_string + ',' + value_var + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['while'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc = Blockly.JavaScript.valueToCode(block, 'proc', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'while(' + value_bool + ',' + value_proc + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['if'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc_1 = Blockly.JavaScript.valueToCode(block, 'proc_1', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc_2 = Blockly.JavaScript.valueToCode(block, 'proc_2', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'if(' + value_bool + ',' + value_proc_1 + ',' + value_proc_2 + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['and'] = function(block) {
  var value_proc = Blockly.JavaScript.valueToCode(block, 'proc', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc_1 = Blockly.JavaScript.valueToCode(block, 'proc_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'and(' + value_proc + ',' + value_proc_1 + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['or'] = function(block) {
  var value_proc = Blockly.JavaScript.valueToCode(block, 'proc', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc_1 = Blockly.JavaScript.valueToCode(block, 'proc_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'or(' + value_proc + ',' + value_proc_1 + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['nothing'] = function(block) {
  // TODO: Assemble JavaScript into code variable.
  var code = 'nothing()';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['global'] = function(block) {
  var value_proc0 = Blockly.JavaScript.valueToCode(block, 'proc0', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = 'global(' + value_proc0 + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['initialshape'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  // TODO: Assemble JavaScript into code variable.
  var code = 'initialShape(' + value_shape + ')';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['assign'] = function(block) {
  var value_var = Blockly.JavaScript.valueToCode(block, 'var', Blockly.JavaScript.ORDER_ATOMIC);
  var value_alltype = Blockly.JavaScript.valueToCode(block, 'alltype', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "assign(" + value_var + "," + value_alltype + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['seq'] = function(block) {
  var value_proc_1 = Blockly.JavaScript.valueToCode(block, 'proc_1', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc_2 = Blockly.JavaScript.valueToCode(block, 'proc_2', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "seq(" + value_proc_1 + "," + value_proc_2 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['src_1'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_ATOMIC);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc1 = Blockly.JavaScript.valueToCode(block, 'proc1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "src_1(" + value_bool + "," + value_shape + "," + value_shape_1 + "," + value_proc1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['src'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_name = Blockly.JavaScript.valueToCode(block, 'NAME', Blockly.JavaScript.ORDER_ATOMIC);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "src(" + value_bool + "," + value_name + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['sr_1'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_ATOMIC);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  var value_proc1 = Blockly.JavaScript.valueToCode(block, 'proc1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "sr_1(" + value_shape + "," + value_shape_1 + "," + value_proc1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['sr'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_ATOMIC);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "sr(" + value_shape + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['id'] = function(block) {
  var text_id = block.getFieldValue('id');
  // TODO: Assemble JavaScript into code variable.
  var code = "id(" + text_id + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['opposite'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_nat = Blockly.JavaScript.valueToCode(block, 'nat', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "opposite(" + value_shape + "," + value_nat + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['oncorner'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var dropdown_corner = block.getFieldValue('corner');
  // TODO: Assemble JavaScript into code variable.
  var code = "oncorner(" + value_shape + ", " + dropdown_corner + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_ATOMIC];
};

Blockly.JavaScript['onborder'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var dropdown_direction = block.getFieldValue('direction');
  // TODO: Assemble JavaScript into code variable.
  var code = "onborder(" + value_shape + "," + dropdown_direction + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['next'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "next(" + value_shape + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['overlapped'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "overlapped(" + value_shape + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['vmidle'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "vmidle(" + value_shape + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['hmidle'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_shape_1 = Blockly.JavaScript.valueToCode(block, 'shape_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "hmidle(" + value_shape + "," + value_shape_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['equals'] = function(block) {
  var value_alltype = Blockly.JavaScript.valueToCode(block, 'alltype', Blockly.JavaScript.ORDER_ATOMIC);
  var value_alltype_1 = Blockly.JavaScript.valueToCode(block, 'alltype_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "equals(" + value_alltype + "," + value_alltype_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['all'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "all(" + value_shape + "," + value_bool + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['some'] = function(block) {
  var value_shape = Blockly.JavaScript.valueToCode(block, 'shape', Blockly.JavaScript.ORDER_NONE);
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "some(" + value_shape + "," + value_bool + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['not'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "not(" + value_bool + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_ATOMIC];
};

Blockly.JavaScript['or_bool'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_bool_1 = Blockly.JavaScript.valueToCode(block, 'bool_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "or_bool(" + value_bool + "," + value_bool_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['and_bool'] = function(block) {
  var value_bool = Blockly.JavaScript.valueToCode(block, 'bool', Blockly.JavaScript.ORDER_ATOMIC);
  var value_bool_1 = Blockly.JavaScript.valueToCode(block, 'bool_1', Blockly.JavaScript.ORDER_ATOMIC);
  // TODO: Assemble JavaScript into code variable.
  var code = "and_bool(" + value_bool + "," + value_bool_1 + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['shape'] = function(block) {
  var text_shape = block.getFieldValue('shape');
  // TODO: Assemble JavaScript into code variable.
  var code = text_shape;
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_ATOMIC];
};



Blockly.JavaScript['availableshapes'] = function(block) {
  var dropdown_name = block.getFieldValue('NAME');
  // TODO: Assemble JavaScript into code variable.
  var code = "shape(" + dropdown_name + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};




Blockly.JavaScript['existingproc'] = function(block) {
  var text_name = block.getFieldValue('proc');
  // TODO: Assemble JavaScript into code variable.
  var code = "existingproc(" + text_name + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};


Blockly.JavaScript['existingproc0'] = function(block) {
  var text_name = block.getFieldValue('proc0');
  // TODO: Assemble JavaScript into code variable.
  var code = "existingproc0(" + text_name + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};


Blockly.JavaScript['existingproc1'] = function(block) {
  var text_name = block.getFieldValue('proc1');
  // TODO: Assemble JavaScript into code variable.
  var code = "existingproc1(" + text_name + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['existingbool'] = function(block) {
  var text_name = block.getFieldValue('bool');
  // TODO: Assemble JavaScript into code variable.
  var code = "existingbool(" + text_name + ")";
  // TODO: Change ORDER_NONE to the correct strength.
  return [code, Blockly.JavaScript.ORDER_NONE];
};
