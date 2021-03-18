// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
	production: false,
	appsApiUrl: 'http://localhost:9001/appsapi/v1/submissions/steps/',
	apiUrl: 'http://localhost:4200/dashapi/v1',
	VERSION: require('../../package.json').version,
	client_id: 'GECORP_Spotlight_Dev_Client',
	analytics: false,
	instrumentationKey: '5d8d3cb3-c532-47ad-a2d3-28a2118ff2fc',
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
