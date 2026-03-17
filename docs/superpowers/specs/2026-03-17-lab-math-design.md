# Lab Math Design

## Background
- The drive rendering math currently exposes HSV helpers but lacks verified LAB conversions, so consumers do not yet have regression coverage for color distance ordering.
- Task 1 needs deterministic RGB->LAB lightness ordering and a DeltaE sanity check before touching the resolver/integration layers.

## Goals
1. Expand the lab-focused tests so they check that black/white are sorted by lightness and that DeltaE returns smaller distances for similar colors than for warm/cool comparisons.
2. Keep the new math inside CellColorMath, exposing lab(int) and deltaE(LabColor, LabColor) plus minimal helper functions.
3. Confirm the focused test suite fails once the tests are added and then succeeds after the conversions exist.

## Architecture
1. In LabColorMathTest, add a lightness ordering case that asserts CellColorMath.lab(0xFF000000) yields lightness below ~5, white yields above ~95, and white.lightness() exceeds black.lightness().
2. Also add a DeltaE sanity case comparing two similar warm grays against a warm/cool pair, asserting the similar colors stay closer and DeltaE remains positive.
3. Implement CellColorMath.lab by linearizing sRGB channels through srgbChannelToLinear(int), computing XYZ using the D65 matrix, normalizing by the reference white (0.95047F, 1.0F, 1.08883F), and pivoting through labPivot(float) before returning a new LabColor.
4. Provide deltaE that computes the Euclidean distance over lightness, a, and b, and expose clampUnit(float) for safe normalization.

## Testing
- Run ./gradlew.bat test --tests com.zzhalex233.ae2cellrender.client.drive.CellColorMathTest --tests com.zzhalex233.ae2cellrender.client.drive.LabColorMathTest immediately after adding the new tests to confirm they fail before the conversion code lands.
- Implement the conversion helpers and rerun the same focused command to prove the math passes the lightness and DeltaE assertions.
