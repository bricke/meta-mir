/*
 * libxml++ compatibility shim using libxml2.
 *
 * Implements the subset of the libxml++ 2.6 API used by mir_wayland_generator,
 * so the generator can be compiled with libxml2 (available as libxml2-native in
 * Yocto) instead of the hand-maintained libxml++-2.6 recipe.
 *
 * Intentionally leaks heap objects: the generator is a short-lived process.
 */
#pragma once

#include <libxml/parser.h>
#include <libxml/tree.h>
#include <libxml/xpath.h>

#include <string>
#include <vector>
#include <stdexcept>

namespace xmlpp {

class Element;
using NodeSet = std::vector<Element*>;

class Node {
public:
    explicit Node(xmlNodePtr n) : n_(n) {}
    virtual ~Node() = default;

    std::string get_name() const
    {
        return n_ ? reinterpret_cast<const char*>(n_->name) : "";
    }

    std::string get_attribute_value(const std::string& name) const
    {
        if (!n_) return "";
        xmlChar* val = xmlGetProp(n_, reinterpret_cast<const xmlChar*>(name.c_str()));
        if (!val) return "";
        std::string result(reinterpret_cast<const char*>(val));
        xmlFree(val);
        return result;
    }

    Node* get_parent() const;

    /* Return child elements whose tag matches name, or all child elements if
     * name is empty. Text nodes and other non-element nodes are skipped. */
    NodeSet get_children(const std::string& name = "") const;

    /* Evaluate an XPath expression relative to this node. */
    NodeSet find(const std::string& xpath) const;

protected:
    xmlNodePtr n_;
};

class Element : public Node {
public:
    explicit Element(xmlNodePtr n) : Node(n) {}
};

class Document {
public:
    explicit Document(xmlDocPtr doc) : doc_(doc) {}

    Element* get_root_node() const
    {
        return new Element(xmlDocGetRootElement(doc_));
    }

private:
    xmlDocPtr doc_;
};

class DomParser {
public:
    explicit DomParser(const std::string& filename)
        : doc_(xmlParseFile(filename.c_str()))
    {
        if (!doc_)
            throw std::runtime_error("xmlpp_compat: failed to parse " + filename);
    }

    ~DomParser() { xmlFreeDoc(doc_); }

    Document* get_document() const { return new Document(doc_); }

private:
    xmlDocPtr doc_;
};

/* --- inline implementations --- */

inline Node* Node::get_parent() const
{
    xmlNodePtr p = n_ ? n_->parent : nullptr;
    if (!p || p->type != XML_ELEMENT_NODE) return nullptr;
    return new Element(p);
}

inline NodeSet Node::get_children(const std::string& name) const
{
    NodeSet result;
    if (!n_) return result;
    for (xmlNodePtr c = n_->children; c; c = c->next)
    {
        if (c->type != XML_ELEMENT_NODE) continue;
        if (name.empty() || name == reinterpret_cast<const char*>(c->name))
            result.push_back(new Element(c));
    }
    return result;
}

inline NodeSet Node::find(const std::string& xpath) const
{
    NodeSet result;
    if (!n_) return result;

    xmlXPathContextPtr ctx = xmlXPathNewContext(n_->doc);
    if (!ctx) return result;
    ctx->node = n_;

    xmlXPathObjectPtr obj = xmlXPathEvalExpression(
        reinterpret_cast<const xmlChar*>(xpath.c_str()), ctx);

    if (obj)
    {
        if (obj->type == XPATH_NODESET && obj->nodesetval)
        {
            for (int i = 0; i < obj->nodesetval->nodeNr; i++)
            {
                xmlNodePtr node = obj->nodesetval->nodeTab[i];
                if (node->type == XML_ELEMENT_NODE)
                    result.push_back(new Element(node));
            }
        }
        xmlXPathFreeObject(obj);
    }
    xmlXPathFreeContext(ctx);
    return result;
}

} // namespace xmlpp
