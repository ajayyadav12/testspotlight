import {
  Component,
  OnInit,
  AfterViewInit,
  ViewEncapsulation,
} from "@angular/core";
import { SessionService } from "../core/session/session.service";
import { SidebarService } from "../core/sidebar/sidebar.service";
import { MenuItem } from "primeng/api/menuitem";
import { SubmissionsService } from "../modules/reports/submissions/submissions.service";
import { SubmissionsComponent } from "../modules/reports/submissions/submissions.component";
import { RxStomp } from "@stomp/rx-stomp";
import * as SockJS from "sockjs-client";
import { map } from "rxjs/operators";
import { AnyRecord } from "dns";
import { getLocaleTimeFormat } from "@angular/common";
import { OverlayPanel } from "primeng/overlaypanel";
import { SelectItem } from "primeng/api/selectitem";
import { Router, ActivatedRoute } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { NotificationService } from "../modules/admin/notification/notification.service";
import { TieredMenuModule } from 'primeng/tieredmenu';
import { Pipe, PipeTransform } from "@angular/core";
import { GuidedTourService } from '../core/services/guided-tour.service';

@Component({
  selector: "app-topbar",
  templateUrl: "./topbar.component.html",
  styleUrls: ["./topbar.component.scss"],
  providers: [NotificationService],
  encapsulation: ViewEncapsulation.None,
})
export class TopbarComponent implements OnInit, AfterViewInit, PipeTransform {
  items: MenuItem[];
  helpItems: MenuItem[];

  //delayedListNotify: SelectItem[];
  delayedListNotification = [];
  delayedListCount = null;
  ssoRole;

  get isLoggedIn(): boolean {
    if (this.sessionSvc.token) {
      return true;
    } else {
      return false;
    }
  }

  get name(): string {
    return this.sessionSvc.name;
  }

  get sso(): string {
    return this.sessionSvc.sso;
  }

  get role(): string {
    return this.sessionSvc.role;
  }

  get listPage(): string {
    return "/" + location.pathname.split("/")[1];
  }

  get isDtlPage(): boolean {
    return location.pathname.match("[0-9]") != null;
  }

  constructor(
    public sessionSvc: SessionService,
    public sidebarSvc: SidebarService,
    public notificationSvc: NotificationService,
    public router: Router,
    private activatedRoute: ActivatedRoute,
    private guidedTourSvc: GuidedTourService
  ) { }

  ngOnInit(): void {

    this.helpItems = [
      {
        label: 'Documentation',
        //icon: 'pi pi-fw pi-file',
        command: (e) => {
          window.open(
            "https://devcloud.swcoe.ge.com/devspace/display/ULBSP/Spotlight+Home"
          );
        },
      },
      {
        label: 'Release Notes',
        //icon: 'pi pi-fw pi-pencil',
        command: (e) => {
          window.open("https://devcloud.swcoe.ge.com/devspace/display/ULBSP/Release+Notes");
        },
      },
      {
        label: 'Guided Tour',
        // icon: 'pi pi-fw pi-calendar',
        items: [
          {
            label: "Relationship View - Coming Soon"
          }
        ]
      },
    ];

    this.items = [
      {
        label: "Incident (Support Central)",
        command: (e) => {
          window.open(
            "https://supportcentral.ge.com/ProcessMaps/form_new_request.asp?prod_id=1163011&form_id=1488144&node_id=3527834&map_id=&reference_id=&reference_type="
          );
        },
      },
      {
        label: "Enhancement Idea (Aha!)",
        command: (e) => {
          window.open("https://financeit-r2r.ideas.aha.io/ideas/new");
        },
      },
      {
        label: "See your tickets (Support Central)",
        command: (e) => {
          window.open(
            "https://supportcentral.ge.com/dataforms/sup_dataform_excelview.asp?prod_id=1163011&form_id=1496809"
          );
        },
      },
    ];
  }

  ngAfterViewInit(): void {
    this.activatedRoute.queryParams.subscribe((params) => {
      if (
        null != this.sessionSvc.sso &&
        this.sessionSvc.sso != "undefined" &&
        this.sessionSvc.sso != ""
      ) {
        this.ssoRole = this.sessionSvc.sso + "," + this.sessionSvc.role;
        this.disconnectClicked();
        this.connectClicked();
        this.getUserNotifications();
      }
    });
  }

  getUserNotifications() {
    this.notificationSvc.getUserNotifications().subscribe((value: any) => {
      this.setValues(value);
    });
  }

  logOut() {
    this.sessionSvc.logout();
  }

  private client: RxStomp;

  connectClicked() {
    if (!this.client || this.client.connected) {
      this.client = new RxStomp();
      this.client.configure({
        webSocketFactory: () => new SockJS("/dashapi/v1/notifySocket"),
        //reconnectDelay: 40000,
        debug: (msg: string) => console.log(msg),
      });
      this.client.activate();

      this.watchForNotifications();
      this.startClicked();
      console.info("connected!");
    }
  }

  private watchForNotifications() {
    this.client
      .watch("/user/notification/item")
      .pipe(
        map((response) => {
          let delayedList = JSON.parse(response.body);
          return delayedList;
        })
      )
      .subscribe((res) => {
        this.setValues(res);
      });
  }

  public setValues(res) {
    let resSTR = JSON.stringify(res);
    let resJSON = JSON.parse(resSTR);
    console.log("resJSON.length::" + resJSON["delayedList"].length);
    this.delayedListNotification = [];
    this.delayedListCount = "";
    var j = 0;

    for (var i = 0; i < resJSON["delayedList"].length; i++) {
      const startTime = new Date(resJSON["delayedList"][i]["startTime"]);
      const endTime = new Date(resJSON["delayedList"][i]["endTime"]);
      this.delayedListNotification.push({
        id: resJSON["delayedList"][i]["submissionId"],
        processname: resJSON["delayedList"][i]["processName"],
        processtype: resJSON["delayedList"][i]["processType"],
        status: resJSON["delayedList"][i]["status"],
        uniqueId: resJSON["delayedList"][i]["scheduleId"],
        notificationDate: this.transform(
          resJSON["delayedList"][i]["startTime"]
        ),
        scheduleDefID: resJSON["delayedList"][i]["scheduledefId"],
        startTime: startTime.toLocaleString(),
        endTime: endTime.toLocaleString(),
      });

      if (resJSON["delayedList"][i]["status"] == "UnRead") {
        ++j;
      }
    }

    this.delayedListCount = j;
    if (this.delayedListCount == 0) {
      this.delayedListCount = "";
    }
  }

  public notificationValue(submissionId, processType, scheduleDefID, uniqueId) {
    if (submissionId != 0) {
      const queryParams = {
        level: "PR",
        searchBy: "dateRange",
        id: submissionId,
      };

      this.router.navigate(["/submissions"], {
        queryParams: queryParams,
        queryParamsHandling: "merge",
      });
    } else if (processType == "Delayed") {
      const queryParams = {
        delayed: "delayed",
        uniqueId: uniqueId,
      };
      this.router.navigate(["/schedule/" + scheduleDefID], {
        queryParams: queryParams,
      });
    }
  }

  public saveNotification(uniqueId, status) {
    if (status == "UnRead") {
      //this.delayedListCount = this.delayedListCount - 1;
      if (this.delayedListCount == 0) {
        this.delayedListCount = "";
      }
      document.getElementById(uniqueId).style.backgroundColor = "white";
      this.notificationSvc
        .updateUserNotification(uniqueId)
        .subscribe((value) => { });
    }
  }

  startClicked() {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: "/swns/start",
        headers: { receipt: this.ssoRole },
      });
    }
  }

  disconnectClicked() {
    if (this.client && this.client.connected) {
      this.client.deactivate();
      this.client = null;
    }
  }

  transform(value: any): any {
    if (value) {
      const differenceInSeconds = Math.floor(
        (+new Date() - +new Date(value)) / 1000
      );
      // less than 30 seconds ago will show as 'Just now'
      if (differenceInSeconds < 30) {
        return "Just now";
      }
      // All values are in seconds
      const timeIntervals = {
        year: 31536000,
        month: 2592000,
        week: 604800,
        day: 86400,
        hour: 3600,
        minute: 60,
        second: 1,
      };
      let counter;
      for (const i in timeIntervals) {
        counter = Math.floor(differenceInSeconds / timeIntervals[i]);
        if (counter > 0) {
          if (counter === 1) {
            // singular (1 day ago)
            return counter + " " + i + " ago";
          } else {
            // plural (2 days ago)
            return counter + " " + i + "s ago";
          }
        }
      }
    }
    return value;
  }
}
