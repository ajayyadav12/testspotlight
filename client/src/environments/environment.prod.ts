export const environment = {
  production: true,
  appsApiUrl: 'http://cihauspzone001.corporate.ge.com:8081/appsapi/v1/submissions/steps/',
  apiUrl: 'https://spotlight.corporate.ge.com/dashapi/v1',
  VERSION: require('../../package.json').version,
  response_type: 'code',
  client_id: 'GECORP_Spotlight_Prod_Client',
  redirect_uri: 'https://spotlight.corporate.ge.com/login',
  scope: 'profile openid api',
  authURL: 'https://fssfed.ge.com/fss/as/authorization.oauth2',
  client_secret: 'ZmluYW5jZWl0cHJvZA==',
  grant_type: 'authorization_code',
  tokenURL: 'https://fssfed.ge.com/fss/as/token.oauth2',
  logOutURL:
    'https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp?referrer=https://spotlight.corporate.ge.com/login?logout=true',
  tokenValidateURL: 'https://api.ge.com/digital/sso/token/validate'
};
