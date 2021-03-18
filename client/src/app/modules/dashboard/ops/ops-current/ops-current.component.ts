
import {Component, OnInit, Input} from '@angular/core';
//import {Car} from '../../components/domain/car';
//import {CarService} from '../../service/carservice';

import {LazyLoadEvent} from 'primeng/api';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { SubmissionsService } from 'src/app/modules/reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-ops-current',
  templateUrl: './ops-current.component.html',
  styleUrls: ['./ops-current.component.scss']
})
export class OpsCurrentComponent implements OnInit {
  @Input() uniqueId = '';
  submissions = [];
  loading = false;
  statuses : any[];

  cols: any[];
  params;
  systemId;

  constructor(		private submissionsSvc: SubmissionsService, 
                  private activatedRoute: ActivatedRoute) {

                    this.activatedRoute.queryParams.subscribe(params => {
                      this.systemId = params.systemId ? params.systemId : this.systemId;
                      this.getCurrentSubmissionStatuses(params);
                    });
              }

  ngOnInit() {    
      this.cols = [
          {field: 'processname', header: 'Etiqueta'},
          {field: 'status', header: 'Valor'}
      ];

    this.statuses = [
      {id: 'Success', status: 'success'},
      {id: 'In Progress', status: 'in progress'},
      {id: 'Warning', status: 'warning'},
      {id: 'Failed', status: 'failed'}     
  ];

    this.getCurrentSubmissionStatuses(this.params);
  }

	ngAfterViewInit(): void {

        
		this.activatedRoute.queryParams.subscribe(params => {
			this.systemId = params.systemId ? params.systemId : this.systemId;
      this.getCurrentSubmissionStatuses(this.params);
		});
	}

  	submissionStatusColor(status) {
      return SubmissionCommon.submissionStatusColor(status);
    }

    getCurrentSubmissionStatuses(tparams: any) {
      this.params = {
        systemId: tparams.systemId ? tparams.systemId : '-1'
      };

      const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
      const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
      this.loading = true;
      this.submissionsSvc.getSubmissionCurrentStatus(this.params).subscribe(      
        (value: any[]) => {
          this.submissions = [];
          this.loading = true;

          value.forEach(s => {
            if (s[7] == 1) {
              this.submissions.push({
                processid: s[1],
                processname: s[0],
                status: this.statusIdtoDesc(s[4])                         
              });
            }
        });
        this.loading = false;
      });
    }

    statusIdtoDesc(status) {
      let statusDesc;
      switch (status) {
        case 1:
          statusDesc = 'in progress';
          break;
        case 2:
          statusDesc = 'success';
          break;
        case 3:
          statusDesc = 'warning';
          break;
        case 4:
          statusDesc = 'failed';
          break;
         case 5:
            statusDesc = 'fatal';
            break;
         default:
          break;
      }
     
      return statusDesc;
    }
}
