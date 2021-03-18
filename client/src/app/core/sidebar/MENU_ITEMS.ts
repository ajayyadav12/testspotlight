export const MENUITEMS = [
	{
		label: '',
		routerLink: ['/'],
		items: [
			{
				label: 'Dashboard',
				routerLink: ['/dashboard'],
				icon: 'pi pi-home'
			}
		]
	},
	{
		label: '',
		routerLink: ['/'],
		items: [
			{
				id: 'GDA1',
				label: 'Relationship Views',
				routerLink: ['/relationship'],
				icon: 'pi pi-eye'
			}
		]
	},
	{
		label: 'Reports',
		routerLink: ['/'],
		items: [
			{
				label: 'Submissions',
				routerLink: ['/submissions'],
				icon: 'pi pi-table',
				admin: false
			},
			{
				label: 'Analytics',
				routerLink: ['/analytics'],
				icon: 'pi pi-chart-bar',
				admin: false
			},
			{
				label: 'SLA Dashboard',
				routerLink: ['/sla-dashboard'],
				icon: 'pi pi-angle-double-right',
				admin: false
			}
		]
	},
	{
		label: 'Interface',
		routerLink: ['/'],
		items: [
			{
				label: 'Process',
				routerLink: ['/process'],
				icon: 'pi pi-sitemap',
				admin: false
			},
			{
				label: 'Schedule',
				routerLink: ['/schedule'],
				icon: 'pi pi-clock',
				admin: false
			},
			{ label: 'System', routerLink: ['/system'], icon: 'pi pi-chevron-circle-up', admin: true }
		]
	},
	{
		label: 'Admin',
		routerLink: ['/'],
		items: [
			{ label: 'Users', routerLink: ['/user'], icon: 'pi pi-user', admin: true },
			{ label: 'Templates', routerLink: ['/notification'], icon: 'pi pi-copy', admin: true },
			{ label: 'Upload', routerLink: ['/upload'], icon: 'pi pi-upload', admin: true }
		]
	},
	// Currently generating extra spacing ,so disabled and assigned to Admin role for Now,once confirmed,can be moved.
	/* {
		label: '',
		routerLink: ['/'],
		items: [
			{
				label: 'Upload',
				routerLink: ['/upload'],
				icon: 'pi pi-upload'
			}
		]
	} */
];
