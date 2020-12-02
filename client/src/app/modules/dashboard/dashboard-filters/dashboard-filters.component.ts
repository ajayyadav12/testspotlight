import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { GEChipFilter } from './GEChipFilter';

const DAYS_DEFAULT = 7;

@Component({
  selector: 'app-dashboard-filters',
  templateUrl: './dashboard-filters.component.html',
  styleUrls: ['./dashboard-filters.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DashboardFiltersComponent implements OnInit {
  filterSelection = ['processes', 'bu', 'adHoc'];
  displayFilters = false;
  days = DAYS_DEFAULT;
  daysOptions = [
    { label: 'Last 7 days', value: 7 },
    { label: 'Last 14 days', value: 14 },
    { label: 'Last 30 days', value: 30 },
    { label: 'Last 90 days', value: 90 }
  ];
  params;
  chipFilters: GEChipFilter[] = [];
  view;
  constructor(private router: Router, private activatedRoute: ActivatedRoute) { }

  get existChipFilters() {
    return this.chipFilters ? this.chipFilters.length > 0 : false;
  }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.params = params;
      this.days = this.params.days ? Number.parseInt(this.params.days) : this.days;
      try {
        const lsChipFilters = JSON.parse(JSON.parse(localStorage.getItem('submission-chip-filters')));
        this.chipFilters = Object.keys(this.params).length ? lsChipFilters : [];
      } catch (error) {
        // Do nothing
      }
    });
  }

  onClickRefresh() {
    this.router.navigate(['/dashboard'], {
      queryParams: { refresh: Math.random() * 10 },
      queryParamsHandling: 'merge'
    });
  }

  /**
   * Update 'days' value from URL query params when user's change value from dropdown
   * @param event
   */
  onChangeDays(event) {
    const queryParams = {
      days: this.days
    };
    this.router.navigate(['/dashboard'], { queryParams: queryParams, queryParamsHandling: 'merge' });
  }

  /**
   * Get new chip filters, set view dropdown to null and update local storage
   * @param chipFilters
   */
  onChangeParams(chipFilters) {
    this.chipFilters = chipFilters;
    this.view = null;
  }

  /**
   * Remove value from URL query params when user's remove chips
   * @param event
   */
  onRemoveFilter(event) {
    const param = event.value;
    let newParam: any = {};
    Object.assign(newParam, this.activatedRoute.snapshot.queryParams);

    if (['childId', 'parentId', 'sender', 'receiver'].some(x => x === param.paramName)) {
      newParam[param.paramName] = newParam[param.paramName].replace(param.id + ',', '');
    } else {
      newParam[param.paramName] = null;
    }

    newParam[param.paramName] = '' ? null : newParam[param.paramName];

    localStorage.setItem('submission-chip-filters', JSON.stringify(this.chipFilters));
    this.router.navigate(['/dashboard'], { queryParams: newParam, queryParamsHandling: 'merge' });
  }
}
