export const environment = {
  production: false,
  appsApiUrl: 'http://alpausdzone001.corporate.ge.com:8081/appsapi/v1/submissions/steps/',
  apiUrl: 'https://spotlight-dev.corporate.ge.com/dashapi/v1',
  VERSION: require('../../package.json').version,
  response_type: 'code',
  client_id: 'GECORP_Spotlight_Dev_Client',
  redirect_uri: 'https://spotlight-dev.corporate.ge.com/mobile/login',
  scope: 'profile openid api',
  authURL: 'https://fssfed.ge.com/fss/as/authorization.oauth2',
  client_secret: 'ZmluYW5jZWl0ZGV2',
  grant_type: 'authorization_code',
  tokenURL: 'https://fssfed.ge.com/fss/as/token.oauth2',
  logOutURL:
    'https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp?referrer=https://spotlight-dev.corporate.ge.com/mobile/login',
  tokenValidateURL: 'https://api.ge.com/digital/sso/token/validate'
};
