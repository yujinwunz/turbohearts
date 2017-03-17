import xml.dom as dom
import xml.dom.minidom as minidom
import sys
import re

def handle_node(node, color, ids):
    for i in node.childNodes:
        handle_node(i, color, ids)
    if node.nodeType == dom.Node.ELEMENT_NODE:
        if node.getAttribute("id") in ids:
            node.setAttribute("fill", color)
            style = node.getAttribute("style")
            if style:
                node.setAttribute("style", re.sub("fill:#[0-9]*", "", style))

if __name__=="__main__":
    if len(sys.argv) != 5:
        print "Usage:"
        print "./" + sys.argv[0] + " <club_id_list> <diamond_id_list> <input.svg> <output_file>"
        exit(1)

    club_ids = dict(map(lambda a: (a, None), filter(None, open(sys.argv[1]).read().split("\n"))))
    diamond_ids = dict(map(lambda a: (a, None), filter(None, open(sys.argv[2]).read().split("\n"))))
    input_filename = sys.argv[3]
    output_filename = sys.argv[4]

    document = minidom.parse(input_filename)
    handle_node(document, "#272FD6", diamond_ids)
    handle_node(document, "#058780", club_ids)
    
    with open(output_filename, "w") as f:
        f.write(document.toprettyxml())
