// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  appsApiUrl: 'http://localhost:9001/appsapi/v1/submissions/steps/',
  apiUrl: 'http://localhost:4200/dashapi/v1',
  VERSION: require('../../package.json').version,
  response_type: 'code',
  client_id: 'GECORP_Spotlight_Dev_Client',
  redirect_uri: 'http://localhost:4200/login',
  scope: 'profile openid api',
  authURL: 'https://fssfed.ge.com/fss/as/authorization.oauth2',
  client_secret: 'ZmluYW5jZWl0ZGV2',
  grant_type: 'authorization_code',
  tokenURL: 'https://fssfed.ge.com/fss/as/token.oauth2',
  logOutURL:
    'https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp?referrer=http://localhost:4200/login?logout=true',
  tokenValidateURL: 'https://api.ge.com/digital/sso/token/validate'
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
