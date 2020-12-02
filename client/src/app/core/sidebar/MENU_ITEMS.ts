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
      { label: 'Sender', routerLink: ['/sender'], icon: 'pi pi-angle-double-right', admin: true },
      {
        label: 'Receiver',
        routerLink: ['/receiver'],
        icon: 'pi pi-angle-double-left',
        admin: true
      }
    ]
  },
  {
    label: 'Admin',
    routerLink: ['/'],
    items: [
      { label: 'Users', routerLink: ['/user'], icon: 'pi pi-user', admin: true },
      { label: 'Templates', routerLink: ['/notification'], icon: 'pi pi-copy', admin: true }
    ]
  }
];
