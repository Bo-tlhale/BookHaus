import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import ConfirmDialog from './ConfirmDialog'

describe('ConfirmDialog', () => {
  it('labels the dialog, moves focus inside, and closes on Escape', () => {
    const onCancel = vi.fn()
    render(<ConfirmDialog open title="Delete shelf?" description="Books stay in your library." onCancel={onCancel} onConfirm={vi.fn()} />)

    const dialog = screen.getByRole('alertdialog', { name: 'Delete shelf?' })
    expect(dialog).toHaveAccessibleDescription('Books stay in your library.')
    expect(screen.getByRole('button', { name: 'Close' })).toHaveFocus()

    fireEvent.keyDown(document, { key: 'Escape' })
    expect(onCancel).toHaveBeenCalledOnce()
  })

  it('keeps keyboard focus within the open dialog', () => {
    render(<ConfirmDialog open title="Delete goal?" description="This cannot be undone." onCancel={vi.fn()} onConfirm={vi.fn()} />)
    const close = screen.getByRole('button', { name: 'Close' })
    const confirm = screen.getByRole('button', { name: 'Confirm' })

    confirm.focus()
    fireEvent.keyDown(document, { key: 'Tab' })
    expect(close).toHaveFocus()

    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true })
    expect(confirm).toHaveFocus()
  })
})
