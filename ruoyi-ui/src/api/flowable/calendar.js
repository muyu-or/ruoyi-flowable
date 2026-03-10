import request from '@/utils/request'

export function getCalendarEvents(year, month) {
  return request({
    url: '/flowable/calendar/events',
    method: 'get',
    params: { year, month }
  })
}

export function backfillPlanStartDate() {
  return request({
    url: '/flowable/calendar/backfill-plan-start-date',
    method: 'post'
  })
}
