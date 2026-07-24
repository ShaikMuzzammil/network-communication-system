#!/usr/bin/env python3
"""Lab 2 Analysis Report Generator"""
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, Table, TableStyle, PageBreak
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
import os

OUTPUT_PATH = "/home/z/my-project/download/SocketComm/docs/lab-reports/lab-2/analysis/Lab2_Analysis_Report.pdf"
SCREENSHOT_DIR = "/home/z/my-project/download/SocketComm/docs/lab-reports/lab-2/screenshots"

def build_document():
    doc = SimpleDocTemplate(OUTPUT_PATH, pagesize=A4, rightMargin=72, leftMargin=72, topMargin=72, bottomMargin=72)
    styles = getSampleStyleSheet()
    
    ps_main = ParagraphStyle(name='MainTitle', parent=styles['Title'], fontSize=24, spaceAfter=30, textColor=colors.HexColor('#1a365d'), alignment=TA_CENTER, fontName='Helvetica-Bold')
    styles.add(ps_main)
    
    ps_sub = ParagraphStyle(name='SubTitle', parent=styles['Heading1'], fontSize=16, spaceBefore=20, spaceAfter=12, textColor=colors.HexColor('#2c5282'), fontName='Helvetica-Bold')
    styles.add(ps_sub)
    
    ps_sec = ParagraphStyle(name='SectionHeading', parent=styles['Heading2'], fontSize=14, spaceBefore=15, spaceAfter=8, textColor=colors.HexColor('#2d3748'), fontName='Helvetica-Bold')
    styles.add(ps_sec)
    
    ps_body = ParagraphStyle(name='CustomBody', parent=styles['Normal'], fontSize=11, leading=16, spaceBefore=6, spaceAfter=6, alignment=TA_JUSTIFY)
    styles.add(ps_body)
    
    story = []
    story.append(Spacer(1, 1*inch))
    story.append(Paragraph("LAB EXPERIMENT ANALYSIS REPORT", styles['MainTitle']))
    story.append(Spacer(1, 0.3*inch))
    story.append(Paragraph("Experiment 2: Introduction to Socket Programming Using Python", styles['SubTitle']))
    story.append(Spacer(1, 0.5*inch))
    
    info_data = [
        ['Student Name:', 'Shaik Muzzammil'],
        ['Roll Number:', 'CH.SC.U4CSE24041'],
        ['Section:', 'CSE-A'],
        ['Course:', '23CSE302 - Computer Networks'],
        ['Institution:', 'Amrita Vishwa Vidyapeetham, Chennai Campus'],
        ['Date of Experiment:', '22/06/2026'],
        ['Maximum Marks:', '10']
    ]
    
    info_table = Table(info_data, colWidths=[2*inch, 4*inch])
    info_table.setStyle(TableStyle([
        ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 11),
        ('TEXTCOLOR', (0, 0), (-1, -1), colors.HexColor('#2d3748')),
        ('ALIGN', (0, 0), (0, -1), 'RIGHT'),
        ('ALIGN', (1, 0), (1, -1), 'LEFT'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 10),
        ('TOPPADDING', (0, 0), (-1, -1), 10),
        ('LINEBELOW', (0, 0), (-1, -1), 0.5, colors.HexColor('#e2e8f0')),
    ]))
    story.append(info_table)
    story.append(PageBreak())
    
    story.append(Paragraph("TABLE OF CONTENTS", styles['SubTitle']))
    toc_items = ["1. Executive Summary", "2. Experiment Objectives", "3. Technical Overview", "4. Implementation Analysis", "5. Code Walkthrough", "6. Screenshot Analysis", "7. Results & Inference", "8. Lab Scenario Solution", "9. Extension Task: Multi-threading", "10. Key Learnings & Takeaways"]
    for item in toc_items:
        story.append(Paragraph(item, styles['CustomBody']))
    
    story.append(PageBreak())
    story.append(Paragraph("1. EXECUTIVE SUMMARY", styles['SubTitle']))
    exec_summary = """This experiment provides a comprehensive hands-on introduction to socket programming using Python, focusing on implementing basic client-server communication architecture. The experiment successfully demonstrates fundamental networking concepts including TCP socket creation, hostname resolution, IP address mapping, and bidirectional data exchange between networked applications."""
    story.append(Paragraph(exec_summary, styles['CustomBody']))
    
    story.append(Paragraph("2. EXPERIMENT OBJECTIVES", styles['SubTitle']))
    objectives = """The primary objectives include gaining proficiency in socket programming concepts, understanding TCP socket creation using Python, mastering hostname resolution techniques, establishing reliable connections between client and server applications, and implementing bidirectional communication channels for data exchange."""
    story.append(Paragraph(objectives, styles['CustomBody']))
    
    story.append(PageBreak())
    story.append(Paragraph("3. TECHNICAL OVERVIEW", styles['SubTitle']))
    story.append(Paragraph("3.1 Socket Programming Fundamentals", styles['SectionHeading']))
    socket_fund = """A socket serves as an endpoint for sending or receiving data across a computer network. This experiment utilizes TCP sockets which provide reliable, connection-oriented communication suitable for chat applications where data integrity is paramount."""
    story.append(Paragraph(socket_fund, styles['CustomBody']))
    
    story.append(Paragraph("4. IMPLEMENTATION ANALYSIS", styles['SubTitle']))
    server_impl = """The server implementation represents the foundational component of our client-server architecture. The ServerApp class extends CustomTkinter's CTk class, encapsulating all socket management functionality within its methods."""
    story.append(Paragraph(server_impl, styles['CustomBody']))
    
    story.append(PageBreak())
    story.append(Paragraph("7. RESULTS & INFERENCE", styles['SubTitle']))
    results = """The experiment achieved all stated objectives successfully. Key achievements include: successful implementation of connection-oriented TCP sockets, proper handling of client-server lifecycle events, real-time message exchange with visual feedback, and extension to multi-client support using Python's threading module."""
    story.append(Paragraph(results, styles['CustomBody']))
    
    # Add screenshots
    story.append(Spacer(1, 0.2*inch))
    story.append(Paragraph("<b>Key Screenshots:</b>", styles['CustomBody']))
    key_pages = [1, 10, 11]
    for page_num in key_pages:
        spath = f"{SCREENSHOT_DIR}/page_{page_num:02d}.png"
        if os.path.exists(spath):
            img = Image(spath, width=5*inch, height=3.5*inch)
            story.append(img)
            story.append(Paragraph(f"Figure: Page {page_num}", styles['CustomBody']))
    
    doc.build(story)
    print(f"Report generated: {OUTPUT_PATH}")
    return OUTPUT_PATH

if __name__ == "__main__":
    build_document()
