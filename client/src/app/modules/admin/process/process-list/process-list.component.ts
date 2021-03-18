import { Component, OnInit } from "@angular/core";
import { ProcessService } from "../process.service";
import { Process } from "../Process";
import { SidebarService } from "src/app/core/sidebar/sidebar.service";
import { MessageService, Message } from "primeng/api";
import { SessionService } from "src/app/core/session/session.service";
import { AuditLogService } from "src/app/core/services/audit-log.service";

@Component({
  providers: [AuditLogService],
  selector: "app-process-list",
  templateUrl: "./process-list.component.html",
})
export class ProcessListComponent implements OnInit {
  processes: Process[];
  columns = [
    { field: "name", header: "Name" },
    { field: "senderName", header: "Sender" },
    { field: "receiverName", header: "Receiver" },
    { field: "processTypeName", header: "Process Type" },
    { field: "processParentName", header: "Process Parent" },
    { field: "appOwnerName", header: "App Owner" },
    { field: "critical", header: "Critical" },
    { field: "isParent", header: "Is Parent" },
  ];
  loading = false;
  showCopyDialog = false;
  selectedProcess: Process;
  selectedValue: any;
  msgs: Message[] = [];
  newProcessDialog: boolean;
  position: string;
  selectedP;

  get isAdmin() {
    return this.sessionSvc.role === "admin";
  }

  get isApplication() {
    return this.sessionSvc.role === "application";
  }

  get isUser() {
    return this.sessionSvc.role === "user";
  }

  constructor(
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private sidebarSvc: SidebarService,
    private sessionSvc: SessionService,
    private auditLogSvc: AuditLogService
  ) {
    this.sidebarSvc.title = "Process";
    this.auditLogSvc.newAuditLog("Process List").subscribe((value) => {});

    //this.selectedValue = (this.sessionSvc.globalProcessName === null) ? null : this.sessionSvc.globalProcessName;
  }

  ngOnInit() {
    this.loading = true;
    this.getProcesses();
  }

  getProcesses() {
    this.processSvc.getAllProcesses(true).subscribe((value) => {
      this.processes = value;
      this.processes.map((p) => {
        p.senderName = p.sender ? p.sender.name : "";
        p.receiverName = p.receiver ? p.receiver.name : "";
        p.processTypeName = p.processType ? p.processType.name : "";
        p.processParentName = p.processParent ? p.processParent.name : "";
        p.appOwnerName = p.appOwner ? p.appOwner.name : "";
        p.iconClass =
          p.approved === "N"
            ? "pi pi-times"
            : p.approved === "0"
            ? "pi pi-clock"
            : "";
        p.iconColor =
          p.approved === "N" ? "red" : p.approved === "0" ? "orange" : "";
      });
      this.loading = false;
      //	this.sessionSvc.globalProcessName = null;
    });
  }

  onDeleteRecord(id) {
    this.loading = true;
    this.processSvc.deleteProcess(id).subscribe((value) => {
      this.processes = this.processes.filter((p) => {
        return p.id !== id;
      });
      this.loading = false;
      //	this.selectedValue = null;
      this.msgSvc.add({
        severity: "error",
        summary: `It's not me, it's you!`,
        detail: `Process was deleted`,
      });
    });
    this.loading = false;
  }

  openNewProcessDialog() {
    this.newProcessDialog = true;
  }

  onChangeProcess(event) {
    this.processes = this.processes.filter((s) => {
      return s.id === event.value.id;
    });
  }

  resetFilters() {
    this.selectedP = 0;
    this.loading = true;
    this.getProcesses();
  }
  onCopyProcess(id): void {
    const process = this.processes.find((p) => p.id === id);
    if (process) {
      this.selectedProcess = process;
      this.showCopyDialog = true;
    }
  }
}
