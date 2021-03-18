import { Component, OnInit } from '@angular/core';
import { SelectItem } from 'primeng/api';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { SubmissionsService } from 'src/app/modules/reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
import * as moment from 'moment-timezone';

@Component({
  selector: 'app-ops-details',
  templateUrl: './ops-details.component.html',
  styleUrls: ['./ops-details.component.scss']
})
export class OpsDetailsComponent implements OnInit {
  submissions = [];
  loading = false;
  cols: any[];

  yearFilter: number;

  yearTimeout: any;
  systemId;
  params;

 
  constructor(		private submissionsSvc: SubmissionsService, 
    private activatedRoute: ActivatedRoute) {

      this.activatedRoute.queryParams.subscribe(params => {
        this.systemId = params.systemId ? params.systemId : this.systemId;
        this.getCurrentSubmissionStatuses(params);
      });
}

  ngOnInit() {

      this.cols = [
          { field: 'id', header: 'ID' },
          { field: 'process', header: 'Process' },
          { field: 'latest', header: 'Latest Run Status' },
          { field: 'prior', header: 'Prior Run Status' },
          { field: 'next', header: 'Next Schedule Run' },
          { field: 'remaining', header: 'Remaining Runs' },
          { field: 'last24', header: 'Last 5 Runs' }  
      ];

      this.getCurrentSubmissionStatuses(this.params);
  }

  onYearChange(event, dt) {
      if (this.yearTimeout) {
          clearTimeout(this.yearTimeout);
      }

      this.yearTimeout = setTimeout(() => {
          dt.filter(event.value, 'year', 'gt');
      }, 250);
  } 

  submissionStatusColor(status) {
    return SubmissionCommon.submissionStatusColor(status);
  }

  getCurrentSubmissionStatuses(tparams: any) {
    this.params = {
      systemId: tparams.systemId ? tparams.systemId : '-1'
    };

    this.loading = true;
    this.submissionsSvc.getSubmissionCurrentStatus(this.params).subscribe(      
      (value: any[]) => {


        this.submissions = [];
        this.loading = true;

        value.forEach(s => {
          moment.tz.setDefault('America/New_York');

          if (s[7] == 1) {
            this.submissions.push({
              id: s[1],
              process: s[0],
              latest: this.statusIdtoDesc(s[4]),
              prior: this.statusIdtoDesc(s[12]),                                       
              next: this.convertToEST(s[9]),
              remaining: s[8],
              second: this.statusIdtoDesc(s[12]),
              third: this.statusIdtoDesc(s[13]),
              fourth: this.statusIdtoDesc(s[14]),
              fifth: this.statusIdtoDesc(s[15]),
              actualStart: this.convertToEST(s[2]),
              actualEnd: this.convertToEST(s[3]),
              schedStart: this.convertToEST(s[5]),
              schedEnd: this.convertToEST(s[6]),
              elapsed: this.seconds_to_string(s[2], s[3]),
              actualStartPrior: '',
              actualEndPrior: '',
              schedStartPrior: '',
              schedEndPrior: '',
              elapsedPrior: '',
              actualStartL3: '',
              actualEndL3: '',
              schedStartL3: '',
              schedEndL13: '',
              elapsedL3: '',              
              actualStartL4: '',
              actualEndL4: '',
              schedStartL4: '',
              schedEndL4: '',
              elapsedL4: '',              
              actualStartL5: '',
              actualEndL5: '',
              schedStartL5: '',
              schedEndL5: '',
              elapsedL5: '',              
            });

          } else if (s[7] == 2) {
            let priorIndex = this.submissions.findIndex(x => x.id ===  s[1]);
            
            this.submissions[priorIndex].actualStartPrior= this.convertToEST(s[2]);
            this.submissions[priorIndex].actualEndPrior= this.convertToEST(s[3]);
            this.submissions[priorIndex].schedStartPrior= this.convertToEST(s[5]);
            this.submissions[priorIndex].schedEndPrior= this.convertToEST(s[6]);
            this.submissions[priorIndex].elapsedPrior= this.seconds_to_string(s[2], s[3]);
          } else if (s[7] == 3) {
            let priorIndex = this.submissions.findIndex(x => x.id ===  s[1]);
            
            this.submissions[priorIndex].actualStartL3= this.convertToEST(s[2]);
            this.submissions[priorIndex].actualEndL3= this.convertToEST(s[3]);
            this.submissions[priorIndex].schedStart3= this.convertToEST(s[5]);
            this.submissions[priorIndex].schedEndL3= this.convertToEST(s[6]);
            this.submissions[priorIndex].elapsedL3= this.seconds_to_string(s[2], s[3]);
          } else if (s[7] == 4) {
            let priorIndex = this.submissions.findIndex(x => x.id ===  s[1]);
            
            this.submissions[priorIndex].actualStartL4= this.convertToEST(s[2]);
            this.submissions[priorIndex].actualEndL4= this.convertToEST(s[3]);
            this.submissions[priorIndex].schedStart4= this.convertToEST(s[5]);
            this.submissions[priorIndex].schedEndL4= this.convertToEST(s[6]);
            this.submissions[priorIndex].elapsedL4= this.seconds_to_string(s[2], s[3]);
          } else if (s[7] == 5) {
            let priorIndex = this.submissions.findIndex(x => x.id ===  s[1]);
            
            this.submissions[priorIndex].actualStartL5= this.convertToEST(s[2]);
            this.submissions[priorIndex].actualEndL5= this.convertToEST(s[3]);
            this.submissions[priorIndex].schedStart5= this.convertToEST(s[5]);
            this.submissions[priorIndex].schedEndL5= this.convertToEST(s[6]);
            this.submissions[priorIndex].elapsedL5= this.seconds_to_string(s[2], s[3]);
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

  convertToEST (date: any) {
    let tempMoment = moment(date);
    let nextRun = '';
    if (date) {
      nextRun = tempMoment.tz('America/New_York').format('MM/DD/YY hh:mm a');
    }  
    return nextRun;
  }

  seconds_to_string(date1, date2) {

    const startTime = new Date(date1);
    const endTime = new Date(date2);
    let duration = (endTime.getTime() - startTime.getTime()) / 1000;

		// day, h, m and s
		const days = Math.floor(duration / (24 * 60 * 60));
		duration -= days * (24 * 60 * 60);
		const hours = Math.floor(duration / (60 * 60));
		duration -= hours * (60 * 60);
		const minutes = Math.floor(duration / 60);
		duration -= minutes * 60;
		return (0 < days ? days + ' day, ' : '') + (0 < hours ? hours + ' hour, ' : '') + minutes + ' mins and ' + Math.trunc(duration) + 's';
	}
}
