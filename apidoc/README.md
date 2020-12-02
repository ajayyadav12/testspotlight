### Swagger UI

This is a static web application and you only need to run a web server to start using it:

- With Node.js: `npm install -g http-server && http-server` and Swagger UI will be served from http://localhost:8082.
- With Python 2.7: `python -m SimpleHTTPServer` and Swagger UI will be served from http://localhost:8000.
- With Python 3.x: `python3 -m http.server` and Swagger UI will be server from http://localhost:8000.

### How to use it

By default you will see dashboard api. To change to applications api just change `dashboard-openapi.yaml` to `applications-openapi.yaml` and click `explore`.

### Troubleshooting

Browsers usually put json requests in cache, if you just git-pulled and the changes are not shown in Swagger UI, please use an incognito tab. 