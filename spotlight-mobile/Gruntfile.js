const fs = require('fs');

module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-war');

    grunt.initConfig({
        war: {
            target: {
                options: {
                    war_verbose: true,
                    war_dist_folder: 'war',
                    war_name: 'spotlight-mobile',
                    webxml_welcome: 'index.html',
                    webxml_display_name: 'Spotlight',
                    war_extras: [{
                        filename: 'WEB-INF/jboss-web.xml',
                        data: function (opts) {
                            return fs.readFileSync('./jboss-web.xml', 'binary');
                        }
                    }]
                },
                files: [{
                    expand: true,
                    cwd: 'dist/spotlight-mobile',
                    src: ['**'],
                    dest: ''
                }]
            }
        }
    });

}