import { Sender } from '../sender/Sender';
import { Receiver } from '../receiver/Receiver';
import { GETable } from 'src/app/shared/ge-table/GETable';

export interface Process extends GETable {
  id: number;
  name: string;
  sender_id: number;
  sender: Sender;
  receiver: Receiver;
  processType: any;
  receiverName: string;
  senderName: string;
  processTypeName: string;
  processParent: any;
  processParentName: string;
  technical_owner_id: number;
  functional_owner_id: number;
  critical_flag: boolean;
  appOwnerName: string;
  appOwner: any;
  approved: String;
}
