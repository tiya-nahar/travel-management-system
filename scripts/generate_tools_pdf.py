from pathlib import Path
import textwrap

PAGE_WIDTH = 595
PAGE_HEIGHT = 842
MARGIN_X = 54
TOP_MARGIN = 64
BOTTOM_MARGIN = 54


class PdfBuilder:
    def __init__(self):
        self.pages = []
        self.current_page = []
        self.y = PAGE_HEIGHT - TOP_MARGIN
        self.page_number = 1
        self._start_page()

    def _start_page(self):
        self.current_page = []
        self.y = PAGE_HEIGHT - TOP_MARGIN
        self._draw_page_header()

    def _finish_page(self):
        self._draw_page_footer()
        self.pages.append("\n".join(self.current_page))
        self.page_number += 1

    def _draw_page_header(self):
        self.current_page.append("0.04 0.17 0.25 rg")
        self.current_page.append(f"0 {PAGE_HEIGHT - 42} {PAGE_WIDTH} 42 re f")
        self._text("Travel Management System - Tools Guide", MARGIN_X, PAGE_HEIGHT - 27, "F2", 11, (1, 1, 1))

    def _draw_page_footer(self):
        self.current_page.append("0.72 0.78 0.83 RG")
        self.current_page.append(f"{MARGIN_X} 36 m {PAGE_WIDTH - MARGIN_X} 36 l S")
        self._text(f"Page {self.page_number}", PAGE_WIDTH - 96, 20, "F1", 10, (0.35, 0.42, 0.50))

    def add_block(self, kind, text):
        if kind == "title":
            self._ensure_space(84)
            self._text(text, MARGIN_X, self.y, "F2", 24, (0.06, 0.12, 0.18))
            self.y -= 18
            self._text(
                "A simple explanation of the technologies used in the project",
                MARGIN_X,
                self.y,
                "F1",
                12,
                (0.34, 0.42, 0.50),
            )
            self.y -= 26
            self.current_page.append("0.82 0.88 0.92 RG")
            self.current_page.append(f"{MARGIN_X} {self.y} m {PAGE_WIDTH - MARGIN_X} {self.y} l S")
            self.y -= 26
            return

        if kind == "section":
            needed = 28 + self._estimate_lines(text, 14, 52) * 18
            self._ensure_space(needed)
            self._text(text, MARGIN_X, self.y, "F2", 16, (0.07, 0.19, 0.28))
            self.y -= 24
            return

        if kind == "paragraph":
            lines = textwrap.wrap(text, width=84)
            needed = len(lines) * 16 + 8
            self._ensure_space(needed)
            for line in lines:
                self._text(line, MARGIN_X, self.y, "F1", 11, (0.14, 0.18, 0.23))
                self.y -= 15
            self.y -= 8
            return

        if kind == "bullet":
            lines = textwrap.wrap(text, width=76)
            needed = len(lines) * 16 + 4
            self._ensure_space(needed)
            if lines:
                self._text("-", MARGIN_X + 8, self.y, "F2", 12, (0.07, 0.19, 0.28))
                self._text(lines[0], MARGIN_X + 22, self.y, "F1", 11, (0.14, 0.18, 0.23))
                self.y -= 15
                for line in lines[1:]:
                    self._text(line, MARGIN_X + 22, self.y, "F1", 11, (0.14, 0.18, 0.23))
                    self.y -= 15
            self.y -= 4
            return

        if kind == "small":
            lines = textwrap.wrap(text, width=88)
            needed = len(lines) * 14 + 6
            self._ensure_space(needed)
            for line in lines:
                self._text(line, MARGIN_X, self.y, "F1", 10, (0.34, 0.42, 0.50))
                self.y -= 13
            self.y -= 6

    def finish(self):
        self._finish_page()
        return self.pages

    def _ensure_space(self, needed_height):
        if self.y - needed_height < BOTTOM_MARGIN:
            self._finish_page()
            self._start_page()

    def _estimate_lines(self, text, font_size, width_chars):
        return max(1, len(textwrap.wrap(text, width=width_chars)))

    def _text(self, text, x, y, font, size, color):
        r, g, b = color
        escaped = (
            text.replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
        )
        self.current_page.append("BT")
        self.current_page.append(f"/{font} {size} Tf")
        self.current_page.append(f"{r:.3f} {g:.3f} {b:.3f} rg")
        self.current_page.append(f"1 0 0 1 {x} {y} Tm")
        self.current_page.append(f"({escaped}) Tj")
        self.current_page.append("ET")


def parse_markdown(text):
    blocks = []
    lines = text.splitlines()
    paragraph_buffer = []

    def flush_paragraph():
        if paragraph_buffer:
            blocks.append(("paragraph", " ".join(paragraph_buffer).strip()))
            paragraph_buffer.clear()

    for line in lines:
        stripped = line.strip()
        if not stripped:
            flush_paragraph()
            continue

        if stripped.startswith("# "):
            flush_paragraph()
            blocks.append(("title", stripped[2:].strip()))
            continue

        if stripped.startswith("## "):
            flush_paragraph()
            blocks.append(("section", stripped[3:].strip()))
            continue

        if stripped.startswith("- "):
            flush_paragraph()
            blocks.append(("bullet", stripped[2:].strip()))
            continue

        if stripped.startswith("Project type:") or stripped.startswith("Build output:") or stripped.startswith("Main runtime flow:"):
            flush_paragraph()
            blocks.append(("small", stripped))
            continue

        paragraph_buffer.append(stripped)

    flush_paragraph()
    return blocks


def build_pdf_bytes(page_streams):
    objects = []

    def add_object(data):
        objects.append(data)
        return len(objects)

    catalog_id = add_object(None)
    pages_id = add_object(None)
    font_regular_id = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    font_bold_id = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>")

    page_ids = []
    for stream in page_streams:
        content_bytes = stream.encode("latin-1")
        content_obj = f"<< /Length {len(content_bytes)} >>\nstream\n{stream}\nendstream"
        content_id = add_object(content_obj)
        page_obj = (
            "<< /Type /Page /Parent {pages} 0 R /MediaBox [0 0 {w} {h}] "
            "/Resources << /Font << /F1 {f1} 0 R /F2 {f2} 0 R >> >> "
            "/Contents {content} 0 R >>"
        ).format(
            pages=pages_id,
            w=PAGE_WIDTH,
            h=PAGE_HEIGHT,
            f1=font_regular_id,
            f2=font_bold_id,
            content=content_id,
        )
        page_id = add_object(page_obj)
        page_ids.append(page_id)

    kids = " ".join(f"{page_id} 0 R" for page_id in page_ids)
    objects[pages_id - 1] = f"<< /Type /Pages /Count {len(page_ids)} /Kids [ {kids} ] >>"
    objects[catalog_id - 1] = f"<< /Type /Catalog /Pages {pages_id} 0 R >>"

    output = bytearray()
    output.extend(b"%PDF-1.4\n%\xE2\xE3\xCF\xD3\n")

    offsets = [0]
    for index, obj in enumerate(objects, start=1):
        offsets.append(len(output))
        output.extend(f"{index} 0 obj\n".encode("latin-1"))
        output.extend(obj.encode("latin-1"))
        output.extend(b"\nendobj\n")

    xref_start = len(output)
    output.extend(f"xref\n0 {len(objects) + 1}\n".encode("latin-1"))
    output.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode("latin-1"))

    trailer = (
        f"trailer\n<< /Size {len(objects) + 1} /Root {catalog_id} 0 R >>\n"
        f"startxref\n{xref_start}\n%%EOF"
    )
    output.extend(trailer.encode("latin-1"))
    return bytes(output)


def main():
    repo_root = Path(__file__).resolve().parent.parent
    source_path = repo_root / "docs" / "project-tools-guide.md"
    output_path = repo_root / "docs" / "project-tools-guide.pdf"

    markdown_text = source_path.read_text(encoding="utf-8")
    blocks = parse_markdown(markdown_text)

    pdf = PdfBuilder()
    for kind, text in blocks:
        pdf.add_block(kind, text)

    page_streams = pdf.finish()
    pdf_bytes = build_pdf_bytes(page_streams)
    output_path.write_bytes(pdf_bytes)
    print(output_path)


if __name__ == "__main__":
    main()
