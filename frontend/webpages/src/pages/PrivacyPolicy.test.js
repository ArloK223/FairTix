import React from 'react';
import { render, screen } from '@testing-library/react';
import PrivacyPolicy from './PrivacyPolicy';

test('renders privacy policy headings', () => {
  render(<PrivacyPolicy />);
  expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
  expect(screen.getByText('What We Collect')).toBeInTheDocument();
  expect(screen.getByText('Cookies')).toBeInTheDocument();
});
