import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders login screen", () => {
  render(<App />);
  expect(screen.getByText(/Planillas institucionales con una imagen moderna, clara y profesional/i)).toBeInTheDocument();
});
