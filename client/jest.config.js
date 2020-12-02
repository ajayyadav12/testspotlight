const tsJestPreset = require('jest-preset-angular/jest-preset').globals['ts-jest'];

module.exports = {
    globals: {
        'ts-jest': {
            ...tsJestPreset,
            tsConfig: 'src/tsconfig.spec.json'
        }
    },
    "reporters": ["default", "jest-junit"]
};