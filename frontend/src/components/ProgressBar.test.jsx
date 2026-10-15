import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import ProgressBar from './ProgressBar'

describe('ProgressBar', () => {
  it('renders the backend percentage accessibly', () => {
    render(<ProgressBar value={42.5} />)
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '42.5')
    expect(screen.getByText('42.5%')).toBeInTheDocument()
  })

  it('does not invent progress when no percentage exists', () => {
    const { container } = render(<ProgressBar value={null} />)
    expect(container).toBeEmptyDOMElement()
  })
})
