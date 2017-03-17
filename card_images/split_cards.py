import xml.dom as dom
import xml.dom.minidom as minidom
import sys
import os
import re
import shapely.affinity as affinity
import numpy

card_names = []
for s in "chsd":
    for r in list("a23456789") + ["10"] + list("jqk"):
        card_names.append(r+s)


def find_rect(node, transform = numpy.matrix([[1, 0, 0], [0, 1, 0], [0, 0, 1]])):
    if node.nodeType == dom.Node.ELEMENT_NODE:
        t = node.getAttribute("transform")
        if "matrix" in t:
            a, b, c, d, e, f = map(float, t.split("(")[1].split(")")[0].split(",")[:6])
            transform = transform * numpy.array(
                    [[a, c, e],
                     [b, d, f],
                     [0, 0, 1]])
        elif "translate" in t:
            a, b = map(float, t.split("(")[1].split(")")[0].split(",")[:6])
            transform = transform * numpy.array(
                    [[1, 0, +a],
                     [0, 1, +b],
                     [0, 0, 1]])

        if node.nodeName.lower() == "rect":
            inv = numpy.linalg.inv(transform)
            return (
                    node.getAttribute("x"),
                    node.getAttribute("y"),
                    node.getAttribute("width"),
                    node.getAttribute("height"),
                    inv
                    )
    for i in node.childNodes:
        res = find_rect(i, transform)
        if res:
            return res

def handle_node(node, dirname, card_ids):
    if node.nodeType == dom.Node.ELEMENT_NODE:
        if node.getAttribute("id") in card_ids:
            index = card_ids.index(node.getAttribute("id"))
            print "processing " + node.getAttribute("id"), card_names[index] + ".svg"
            x, y, width, height, tf = find_rect(node)
            print tf
            with open(os.path.join(dirname, card_names[index] + ".svg"), "w") as f:

                f.write("""<?xml version="1.0" ?>
                <!-- Created with Inkscape (http://www.inkscape.org/) -->
                <svg id="svg11376" inkscape:version="0.91 r13725" sodipodi:docname="Color_52_Faces_v.2.0.svg" version="1.1" viewBox="%s %s %s %s" width="%s" height="%s" xmlns="http://www.w3.org/2000/svg" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">"""%(float(x)-1, float(y)-1, float(width)+2, float(height)+2, width + "mm", height + "mm") )
                f.write("<g transform=\"matrix(%f,%f,%f,%f,%f,%f)\">"%(tf[0, 0], tf[1, 0], tf[0, 1], tf[1, 1], tf[0,2], tf[1, 2]))
                f.write(node.toprettyxml())
                f.write("""</g></svg>""")
            return            
    for i in node.childNodes:
        handle_node(i, dirname, card_ids)

if __name__=="__main__":
    if len(sys.argv) != 4:
        print "Usage:"
        print "./" + sys.argv[0] + " <card_ids> <input.svg> <output_dir>"
        exit(1)

    card_ids = filter(None, open(sys.argv[1]).read().split("\n"))
    input_filename = sys.argv[2]
    output_dir = sys.argv[3]

    document = minidom.parse(input_filename)
    handle_node(document, output_dir, card_ids)
