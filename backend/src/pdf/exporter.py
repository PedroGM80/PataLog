"""
Genera informes clinicos veterinarios en PDF usando reportlab.
"""

from datetime import datetime
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    HRFlowable,
)
from reportlab.lib.enums import TA_CENTER, TA_RIGHT


def generate_report_pdf(
    output_path: str,
    animal_name: str,
    animal_species: str,
    animal_breed: str,
    owner_name: str,
    owner_phone: str,
    consultation_date: str,
    report: str,
    notes: str = "",
    clinic_name: str = "Clinica Veterinaria",
    vet_license: str = "",  # Numero de colegiado (Sprint 2)
) -> str:
    """
    Genera el PDF del informe clinico y lo guarda en output_path.
    Devuelve la ruta del archivo generado.
    """

    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=2.5 * cm,
        rightMargin=2.5 * cm,
        topMargin=2.5 * cm,
        bottomMargin=2.5 * cm,
        title=f"Informe clinico - {animal_name}",
        author=clinic_name,
    )

    styles = getSampleStyleSheet()
    story = []

    # --- Estilos personalizados ---
    title_style = ParagraphStyle(
        "VetTitle",
        parent=styles["Title"],
        fontSize=18,
        textColor=colors.HexColor("#1a5276"),
        spaceAfter=4,
    )
    subtitle_style = ParagraphStyle(
        "VetSubtitle",
        parent=styles["Normal"],
        fontSize=10,
        textColor=colors.HexColor("#555555"),
        alignment=TA_CENTER,
        spaceAfter=2,
    )
    section_header_style = ParagraphStyle(
        "SectionHeader",
        parent=styles["Heading2"],
        fontSize=11,
        textColor=colors.HexColor("#1a5276"),
        spaceBefore=14,
        spaceAfter=4,
    )
    body_style = ParagraphStyle(
        "VetBody",
        parent=styles["Normal"],
        fontSize=10,
        leading=15,
        spaceAfter=4,
    )
    label_style = ParagraphStyle(
        "Label",
        parent=styles["Normal"],
        fontSize=9,
        textColor=colors.HexColor("#888888"),
    )
    value_style = ParagraphStyle(
        "Value",
        parent=styles["Normal"],
        fontSize=10,
        fontName="Helvetica-Bold",
    )

    # --- Cabecera ---
    story.append(Paragraph(clinic_name, title_style))
    story.append(Paragraph("Informe Clinico Veterinario", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=2, color=colors.HexColor("#1a5276"), spaceAfter=12))

    # --- Tabla de datos del paciente ---
    patient_data = [
        [
            Paragraph("Paciente", label_style),
            Paragraph("Especie / Raza", label_style),
            Paragraph("Propietario", label_style),
            Paragraph("Fecha consulta", label_style),
        ],
        [
            Paragraph(animal_name or "-", value_style),
            Paragraph(f"{animal_species or '-'} / {animal_breed or '-'}", value_style),
            Paragraph(f"{owner_name or '-'}<br/>{owner_phone or ''}", value_style),
            Paragraph(consultation_date or "-", value_style),
        ],
    ]

    patient_table = Table(patient_data, colWidths=["25%", "25%", "30%", "20%"])
    patient_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#eaf4fb")),
        ("BACKGROUND", (0, 1), (-1, 1), colors.white),
        ("BOX", (0, 0), (-1, -1), 0.5, colors.HexColor("#aaaaaa")),
        ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#cccccc")),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ]))
    story.append(patient_table)
    story.append(Spacer(1, 16))

    # --- Informe clinico estructurado ---
    story.append(Paragraph("Informe Clinico", section_header_style))
    story.append(HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#aaaaaa"), spaceAfter=8))

    if report:
        sections = _parse_report_sections(report)
        if sections:
            for section_title, section_content in sections:
                story.append(Paragraph(section_title, section_header_style))
                for line in section_content.strip().splitlines():
                    if line.strip():
                        story.append(Paragraph(line.strip(), body_style))
        else:
            for line in report.splitlines():
                if line.strip():
                    story.append(Paragraph(line.strip(), body_style))

    # --- Notas adicionales ---
    if notes and notes.strip():
        story.append(Spacer(1, 8))
        story.append(Paragraph("Notas adicionales", section_header_style))
        story.append(HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#aaaaaa"), spaceAfter=8))
        for line in notes.splitlines():
            if line.strip():
                story.append(Paragraph(line.strip(), body_style))

    # --- Pie de pagina ---
    story.append(Spacer(1, 24))
    story.append(HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#cccccc"), spaceAfter=6))
    
    generated_at = datetime.now().strftime("%d/%m/%Y %H:%M")
    footer_parts = [f"Documento generado el {generated_at}", clinic_name]
    if vet_license:
        footer_parts.append(f"N. Colegiado: {vet_license}")
    
    footer_style = ParagraphStyle(
        "Footer",
        parent=styles["Normal"],
        fontSize=8,
        textColor=colors.HexColor("#aaaaaa"),
        alignment=TA_RIGHT,
    )
    story.append(Paragraph(" - ".join(footer_parts), footer_style))

    doc.build(story)
    return output_path


def _parse_report_sections(report: str) -> list[tuple[str, str]]:
    """
    Intenta dividir el informe en secciones con formato 'TITULO:\ncontenido'.
    """
    sections = []
    current_title = None
    current_lines = []

    for line in report.splitlines():
        stripped = line.strip()
        if stripped.endswith(":") and stripped == stripped.upper() and len(stripped) > 3:
            if current_title is not None:
                sections.append((current_title, "\n".join(current_lines)))
            current_title = stripped.rstrip(":")
            current_lines = []
        else:
            if current_title is not None:
                current_lines.append(line)

    if current_title is not None:
        sections.append((current_title, "\n".join(current_lines)))

    return sections
