import { Component, OnInit, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-period-and-bu',
  templateUrl: './period-and-bu.component.html',
  styleUrls: ['./period-and-bu.component.scss']
})
export class PeriodAndBuComponent implements OnInit {

  @Input() reportForm: FormGroup;
  @Input() isPeriod;
  businesses = [
    { label: 'Aviation', value: 'AV' },
    { label: 'Corporate', value: 'CO' },
    { label: 'Healthcare', value: 'HC' },
    { label: 'Oil & Gas', value: 'OG' },
    { label: 'Renewables', value: 'RE' },
    { label: 'Total Company', value: 'TC' },
    { label: 'Capital', value: 'CA' },
    { label: 'Power', value: 'PO' },
    { label: 'Industrial', value: 'IN' },
    { label: 'GECSIN', value: 'GN' },
    { label: 'GPINP', value: 'GP' }
  ];

  constructor() { }

  ngOnInit(): void {
  }

}
