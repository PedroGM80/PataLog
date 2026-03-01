"""
Handler para exportacion de PDFs.
"""

from ..protocol import Request, send_ok, send_error, log
from ..pdf.exporter import generate_report_pdf


def handle_export_pdf(request: Request) -> None:
    """Genera un PDF del informe clinico."""
    output_path = request.params.get("output_path", "")
    
    if not output_path:
        send_error(request.id, "Falta el parametro 'output_path'")
        return
    
    log(f"Generando PDF en {output_path}...")
    
    try:
        generate_report_pdf(
            output_path=output_path,
            animal_name=request.params.get("animal_name", ""),
            animal_species=request.params.get("animal_species", ""),
            animal_breed=request.params.get("animal_breed", ""),
            owner_name=request.params.get("owner_name", ""),
            owner_phone=request.params.get("owner_phone", ""),
            consultation_date=request.params.get("consultation_date", ""),
            report=request.params.get("report", ""),
            notes=request.params.get("notes", ""),
            clinic_name=request.params.get("clinic_name", "Clinica Veterinaria"),
            vet_license=request.params.get("vet_license", ""),  # Sprint 2
        )
        log("PDF generado.")
        send_ok(request.id, pdf_path=output_path)
        
    except Exception as e:
        send_error(request.id, f"Error generando PDF: {e}")
