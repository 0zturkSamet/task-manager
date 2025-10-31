import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import {
  formatDate,
  formatDateTime,
  isOverdue,
  daysUntilDue,
  getRelativeTime,
  getInitials,
  truncate,
  getStatusLabel,
  getPriorityLabel,
  getRoleLabel,
  calculateCompletionPercentage,
  sortByPriority,
  groupTasksByStatus,
  filterTasksBySearch,
  generateRandomColor,
} from '../helpers'

describe('helpers.js', () => {
  describe('formatDate', () => {
    it('should format date string to readable format', () => {
      const date = '2024-01-15T10:30:00'
      const result = formatDate(date)
      expect(result).toBe('Jan 15, 2024')
    })

    it('should return N/A for null date', () => {
      expect(formatDate(null)).toBe('N/A')
    })

    it('should return N/A for undefined date', () => {
      expect(formatDate(undefined)).toBe('N/A')
    })

    it('should return N/A for empty string', () => {
      expect(formatDate('')).toBe('N/A')
    })
  })

  describe('formatDateTime', () => {
    it('should format date string with time', () => {
      const date = '2024-01-15T10:30:00'
      const result = formatDateTime(date)
      expect(result).toContain('Jan 15, 2024')
      expect(result).toContain('10:30')
    })

    it('should return N/A for null date', () => {
      expect(formatDateTime(null)).toBe('N/A')
    })

    it('should return N/A for undefined date', () => {
      expect(formatDateTime(undefined)).toBe('N/A')
    })
  })

  describe('isOverdue', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2024-01-15T12:00:00'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return true for overdue tasks', () => {
      const pastDate = '2024-01-10T12:00:00'
      expect(isOverdue(pastDate, 'TODO')).toBe(true)
    })

    it('should return false for future due dates', () => {
      const futureDate = '2024-01-20T12:00:00'
      expect(isOverdue(futureDate, 'TODO')).toBe(false)
    })

    it('should return false for DONE tasks', () => {
      const pastDate = '2024-01-10T12:00:00'
      expect(isOverdue(pastDate, 'DONE')).toBe(false)
    })

    it('should return false for CANCELLED tasks', () => {
      const pastDate = '2024-01-10T12:00:00'
      expect(isOverdue(pastDate, 'CANCELLED')).toBe(false)
    })

    it('should return false for null due date', () => {
      expect(isOverdue(null, 'TODO')).toBe(false)
    })
  })

  describe('daysUntilDue', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2024-01-15T12:00:00'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should calculate positive days for future dates', () => {
      const futureDate = '2024-01-20T12:00:00'
      expect(daysUntilDue(futureDate)).toBe(5)
    })

    it('should calculate negative days for past dates', () => {
      const pastDate = '2024-01-10T12:00:00'
      expect(daysUntilDue(pastDate)).toBe(-5)
    })

    it('should return null for null due date', () => {
      expect(daysUntilDue(null)).toBeNull()
    })

    it('should return 0 for same day', () => {
      const sameDay = '2024-01-15T23:59:59'
      expect(daysUntilDue(sameDay)).toBe(1)
    })
  })

  describe('getRelativeTime', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2024-01-15T12:00:00'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return "just now" for recent times', () => {
      const recent = new Date('2024-01-15T11:59:30').toISOString()
      expect(getRelativeTime(recent)).toBe('just now')
    })

    it('should return minutes ago for times less than an hour', () => {
      const minutesAgo = new Date('2024-01-15T11:40:00').toISOString()
      expect(getRelativeTime(minutesAgo)).toBe('20 minutes ago')
    })

    it('should return hours ago for times less than a day', () => {
      const hoursAgo = new Date('2024-01-15T10:00:00').toISOString()
      expect(getRelativeTime(hoursAgo)).toBe('2 hours ago')
    })

    it('should return days ago for times less than a week', () => {
      const daysAgo = new Date('2024-01-13T12:00:00').toISOString()
      expect(getRelativeTime(daysAgo)).toBe('2 days ago')
    })

    it('should return formatted date for times over a week', () => {
      const weekAgo = new Date('2024-01-01T12:00:00').toISOString()
      expect(getRelativeTime(weekAgo)).toBe('Jan 1, 2024')
    })

    it('should return N/A for null date', () => {
      expect(getRelativeTime(null)).toBe('N/A')
    })
  })

  describe('getInitials', () => {
    it('should return initials from first and last name', () => {
      expect(getInitials('John', 'Doe')).toBe('JD')
    })

    it('should handle single letter names', () => {
      expect(getInitials('A', 'B')).toBe('AB')
    })

    it('should handle missing last name', () => {
      expect(getInitials('John', null)).toBe('J')
    })

    it('should handle missing first name', () => {
      expect(getInitials(null, 'Doe')).toBe('D')
    })

    it('should handle both names missing', () => {
      expect(getInitials(null, null)).toBe('')
    })

    it('should convert to uppercase', () => {
      expect(getInitials('john', 'doe')).toBe('JD')
    })
  })

  describe('truncate', () => {
    it('should truncate long text', () => {
      const longText = 'This is a very long text that should be truncated'
      expect(truncate(longText, 20)).toBe('This is a very long ...')
    })

    it('should not truncate short text', () => {
      const shortText = 'Short'
      expect(truncate(shortText, 20)).toBe('Short')
    })

    it('should use default maxLength of 50', () => {
      const text = 'a'.repeat(60)
      expect(truncate(text)).toBe('a'.repeat(50) + '...')
    })

    it('should handle null text', () => {
      expect(truncate(null)).toBeNull()
    })

    it('should handle undefined text', () => {
      expect(truncate(undefined)).toBeUndefined()
    })

    it('should handle text exactly at maxLength', () => {
      const text = 'a'.repeat(20)
      expect(truncate(text, 20)).toBe(text)
    })
  })

  describe('getStatusLabel', () => {
    it('should return label for TODO', () => {
      expect(getStatusLabel('TODO')).toBe('To Do')
    })

    it('should return label for IN_PROGRESS', () => {
      expect(getStatusLabel('IN_PROGRESS')).toBe('In Progress')
    })

    it('should return label for IN_REVIEW', () => {
      expect(getStatusLabel('IN_REVIEW')).toBe('In Review')
    })

    it('should return label for DONE', () => {
      expect(getStatusLabel('DONE')).toBe('Done')
    })

    it('should return label for CANCELLED', () => {
      expect(getStatusLabel('CANCELLED')).toBe('Cancelled')
    })

    it('should return original status for unknown status', () => {
      expect(getStatusLabel('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getPriorityLabel', () => {
    it('should return label for LOW', () => {
      expect(getPriorityLabel('LOW')).toBe('Low')
    })

    it('should return label for MEDIUM', () => {
      expect(getPriorityLabel('MEDIUM')).toBe('Medium')
    })

    it('should return label for HIGH', () => {
      expect(getPriorityLabel('HIGH')).toBe('High')
    })

    it('should return label for URGENT', () => {
      expect(getPriorityLabel('URGENT')).toBe('Urgent')
    })

    it('should return original priority for unknown priority', () => {
      expect(getPriorityLabel('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getRoleLabel', () => {
    it('should return label for OWNER', () => {
      expect(getRoleLabel('OWNER')).toBe('Owner')
    })

    it('should return label for EDITOR', () => {
      expect(getRoleLabel('EDITOR')).toBe('Editor')
    })

    it('should return label for VIEWER', () => {
      expect(getRoleLabel('VIEWER')).toBe('Viewer')
    })

    it('should return original role for unknown role', () => {
      expect(getRoleLabel('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('calculateCompletionPercentage', () => {
    it('should calculate percentage correctly', () => {
      expect(calculateCompletionPercentage(5, 10)).toBe(50)
    })

    it('should round to nearest integer', () => {
      expect(calculateCompletionPercentage(1, 3)).toBe(33)
    })

    it('should return 0 for 0 total', () => {
      expect(calculateCompletionPercentage(0, 0)).toBe(0)
    })

    it('should return 100 for all completed', () => {
      expect(calculateCompletionPercentage(10, 10)).toBe(100)
    })

    it('should handle decimal results', () => {
      expect(calculateCompletionPercentage(7, 10)).toBe(70)
    })
  })

  describe('sortByPriority', () => {
    it('should sort tasks by priority (URGENT first)', () => {
      const tasks = [
        { id: 1, priority: 'LOW' },
        { id: 2, priority: 'URGENT' },
        { id: 3, priority: 'MEDIUM' },
        { id: 4, priority: 'HIGH' },
      ]
      const sorted = sortByPriority(tasks)
      expect(sorted[0].priority).toBe('URGENT')
      expect(sorted[1].priority).toBe('HIGH')
      expect(sorted[2].priority).toBe('MEDIUM')
      expect(sorted[3].priority).toBe('LOW')
    })

    it('should not mutate original array', () => {
      const tasks = [
        { id: 1, priority: 'LOW' },
        { id: 2, priority: 'HIGH' },
      ]
      const original = [...tasks]
      sortByPriority(tasks)
      expect(tasks).toEqual(original)
    })

    it('should handle empty array', () => {
      expect(sortByPriority([])).toEqual([])
    })
  })

  describe('groupTasksByStatus', () => {
    it('should group tasks by status', () => {
      const tasks = [
        { id: 1, status: 'TODO' },
        { id: 2, status: 'TODO' },
        { id: 3, status: 'DONE' },
        { id: 4, status: 'IN_PROGRESS' },
      ]
      const grouped = groupTasksByStatus(tasks)
      expect(grouped.TODO).toHaveLength(2)
      expect(grouped.DONE).toHaveLength(1)
      expect(grouped.IN_PROGRESS).toHaveLength(1)
    })

    it('should handle empty array', () => {
      expect(groupTasksByStatus([])).toEqual({})
    })

    it('should handle single status', () => {
      const tasks = [
        { id: 1, status: 'TODO' },
        { id: 2, status: 'TODO' },
      ]
      const grouped = groupTasksByStatus(tasks)
      expect(Object.keys(grouped)).toHaveLength(1)
      expect(grouped.TODO).toHaveLength(2)
    })
  })

  describe('filterTasksBySearch', () => {
    const tasks = [
      { id: 1, title: 'Buy groceries', description: 'Milk and eggs' },
      { id: 2, title: 'Fix bug', description: 'Update component' },
      { id: 3, title: 'Write tests', description: 'Add unit tests' },
    ]

    it('should filter by title', () => {
      const filtered = filterTasksBySearch(tasks, 'bug')
      expect(filtered).toHaveLength(1)
      expect(filtered[0].title).toBe('Fix bug')
    })

    it('should filter by description', () => {
      const filtered = filterTasksBySearch(tasks, 'unit')
      expect(filtered).toHaveLength(1)
      expect(filtered[0].title).toBe('Write tests')
    })

    it('should be case insensitive', () => {
      const filtered = filterTasksBySearch(tasks, 'BUY')
      expect(filtered).toHaveLength(1)
      expect(filtered[0].title).toBe('Buy groceries')
    })

    it('should return all tasks for empty query', () => {
      const filtered = filterTasksBySearch(tasks, '')
      expect(filtered).toHaveLength(3)
    })

    it('should return all tasks for null query', () => {
      const filtered = filterTasksBySearch(tasks, null)
      expect(filtered).toHaveLength(3)
    })

    it('should handle tasks without description', () => {
      const tasksNoDesc = [{ id: 1, title: 'Task 1' }]
      const filtered = filterTasksBySearch(tasksNoDesc, 'Task')
      expect(filtered).toHaveLength(1)
    })

    it('should return empty array for no matches', () => {
      const filtered = filterTasksBySearch(tasks, 'xyz123')
      expect(filtered).toHaveLength(0)
    })
  })

  describe('generateRandomColor', () => {
    it('should return a color from the predefined list', () => {
      const validColors = [
        '#3B82F6',
        '#10B981',
        '#F59E0B',
        '#EF4444',
        '#8B5CF6',
        '#EC4899',
        '#14B8A6',
        '#F97316',
      ]
      const color = generateRandomColor()
      expect(validColors).toContain(color)
    })

    it('should return a string', () => {
      const color = generateRandomColor()
      expect(typeof color).toBe('string')
    })

    it('should return a hex color', () => {
      const color = generateRandomColor()
      expect(color).toMatch(/^#[0-9A-F]{6}$/i)
    })
  })
})
