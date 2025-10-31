import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Badge from '../Badge'

describe('Badge', () => {
  describe('status badge', () => {
    it('should render status badge with correct label', () => {
      render(<Badge type="status" value="TODO" />)
      expect(screen.getByText('To Do')).toBeInTheDocument()
    })

    it('should apply correct color for TODO status', () => {
      render(<Badge type="status" value="TODO" />)
      const badge = screen.getByText('To Do')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })

    it('should apply correct color for IN_PROGRESS status', () => {
      render(<Badge type="status" value="IN_PROGRESS" />)
      const badge = screen.getByText('In Progress')
      expect(badge).toHaveClass('bg-blue-100')
      expect(badge).toHaveClass('text-blue-800')
    })

    it('should apply correct color for IN_REVIEW status', () => {
      render(<Badge type="status" value="IN_REVIEW" />)
      const badge = screen.getByText('In Review')
      expect(badge).toHaveClass('bg-yellow-100')
      expect(badge).toHaveClass('text-yellow-800')
    })

    it('should apply correct color for DONE status', () => {
      render(<Badge type="status" value="DONE" />)
      const badge = screen.getByText('Done')
      expect(badge).toHaveClass('bg-green-100')
      expect(badge).toHaveClass('text-green-800')
    })

    it('should apply correct color for CANCELLED status', () => {
      render(<Badge type="status" value="CANCELLED" />)
      const badge = screen.getByText('Cancelled')
      expect(badge).toHaveClass('bg-red-100')
      expect(badge).toHaveClass('text-red-800')
    })

    it('should apply default color for unknown status', () => {
      render(<Badge type="status" value="UNKNOWN" />)
      const badge = screen.getByText('UNKNOWN')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })
  })

  describe('priority badge', () => {
    it('should render priority badge with correct label', () => {
      render(<Badge type="priority" value="LOW" />)
      expect(screen.getByText('Low')).toBeInTheDocument()
    })

    it('should apply correct color for LOW priority', () => {
      render(<Badge type="priority" value="LOW" />)
      const badge = screen.getByText('Low')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })

    it('should apply correct color for MEDIUM priority', () => {
      render(<Badge type="priority" value="MEDIUM" />)
      const badge = screen.getByText('Medium')
      expect(badge).toHaveClass('bg-blue-100')
      expect(badge).toHaveClass('text-blue-800')
    })

    it('should apply correct color for HIGH priority', () => {
      render(<Badge type="priority" value="HIGH" />)
      const badge = screen.getByText('High')
      expect(badge).toHaveClass('bg-orange-100')
      expect(badge).toHaveClass('text-orange-800')
    })

    it('should apply correct color for URGENT priority', () => {
      render(<Badge type="priority" value="URGENT" />)
      const badge = screen.getByText('Urgent')
      expect(badge).toHaveClass('bg-red-100')
      expect(badge).toHaveClass('text-red-800')
    })

    it('should apply default color for unknown priority', () => {
      render(<Badge type="priority" value="UNKNOWN" />)
      const badge = screen.getByText('UNKNOWN')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })
  })

  describe('role badge', () => {
    it('should render role badge with correct label', () => {
      render(<Badge type="role" value="OWNER" />)
      expect(screen.getByText('Owner')).toBeInTheDocument()
    })

    it('should apply correct color for OWNER role', () => {
      render(<Badge type="role" value="OWNER" />)
      const badge = screen.getByText('Owner')
      expect(badge).toHaveClass('bg-purple-100')
      expect(badge).toHaveClass('text-purple-800')
    })

    it('should apply correct color for EDITOR role', () => {
      render(<Badge type="role" value="EDITOR" />)
      const badge = screen.getByText('Editor')
      expect(badge).toHaveClass('bg-blue-100')
      expect(badge).toHaveClass('text-blue-800')
    })

    it('should apply correct color for VIEWER role', () => {
      render(<Badge type="role" value="VIEWER" />)
      const badge = screen.getByText('Viewer')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })

    it('should apply default color for unknown role', () => {
      render(<Badge type="role" value="UNKNOWN" />)
      const badge = screen.getByText('UNKNOWN')
      expect(badge).toHaveClass('bg-gray-100')
      expect(badge).toHaveClass('text-gray-800')
    })
  })

  describe('general', () => {
    it('should apply custom className', () => {
      render(<Badge type="status" value="TODO" className="custom-class" />)
      const badge = screen.getByText('To Do')
      expect(badge).toHaveClass('custom-class')
    })

    it('should have base badge styling classes', () => {
      render(<Badge type="status" value="TODO" />)
      const badge = screen.getByText('To Do')
      expect(badge).toHaveClass('inline-flex')
      expect(badge).toHaveClass('items-center')
      expect(badge).toHaveClass('rounded-full')
      expect(badge).toHaveClass('text-xs')
      expect(badge).toHaveClass('font-medium')
    })

    it('should render as span element', () => {
      const { container } = render(<Badge type="status" value="TODO" />)
      const badge = container.querySelector('span')
      expect(badge).toBeInTheDocument()
    })
  })
})
