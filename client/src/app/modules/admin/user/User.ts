import { GETable } from 'src/app/shared/ge-table/GETable';

export interface User extends GETable {
  id: number;
  sso: string;
  name: string;
  role: any;
  roleName: string;
}
