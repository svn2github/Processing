<?php

function GetContentAsString($node) {   
    $st = "";
    foreach ($node->child_nodes() as $cnode)
        if ($cnode->node_type()==XML_TEXT_NODE)
            $st .= $cnode->node_value();
        else if ($cnode->node_type()==XML_ELEMENT_NODE) {
            $st .= "<" . $cnode->node_name();
            if ($attribnodes=$cnode->attributes()) {
                $st .= " ";
                foreach ($attribnodes as $anode)
                    $st .= $anode->node_name() . "='" .
                        $anode-node_value() . "'";
            }   
            $nodeText = GetContentAsString($cnode);
            if (empty($nodeText) && !$attribnodes)
                $st .= " />";        // unary
            else
                $st .= ">" . $nodeText . "</" .
                    $cnode->node_name() . ">";
        }
    return $st;
}

if (is_null($fullpath)) {
    $fullpath = dirname(__FILE__);
}

$dom = domxml_open_file($fullpath ."/API/". $_GET['name'] .".xml");
$root = $dom->document_element();
$value = array();
$child = $root->first_child();

while ($child) {
    $tag = $child->node_name();
    if (($tag == 'example') || ($tag == 'parameter') ||
        ($tag == 'method') || ($tag == 'cparameter') ||
        ($tag == 'field')) {
        $subvalue = array();
        $gchild = $child->first_child();
        while ($gchild) {
            $gtag = $gchild->node_name();
            $content = trim($gchild->get_content());
            if ($content != "") {
                $subvalue[$gtag] = $content;
            }

            $gchild = $gchild->next_sibling();
        }
        if (count($subvalue) > 0) {
            $value[$tag][] = $subvalue;
        }
    } else if ($tag == 'related') {
        $content = trim(GetContentAsString($child));
        if ($content != "") {
            $content = split("\n", $content);
            foreach ($content as $c) {
                if (file_exists('API/'. rtrim($c, "() ") .'.xml')) {
                    $value[$tag][] = trim($c);
                }
            }
        }        
    } else if ($tag[0] == '#') {
        //// skip
    } else {
        $content = trim(GetContentAsString($child));
        if ($content != "") {
            $value[$tag] = $content;
        }
    }

    $child = $child->next_sibling();
}
?>
    

<table border="0" cellspacing="0" cellpadding="0">
<?php if (isset($value['name'])) { ?>
  <tr>
    <td class="reffieldheader">Name</td>
    <td class="reffield">
      <h3><?php echo $value['name'] ?></h3>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['example'])) { ?>
  <tr>
    <td class="reffieldheader">Examples</td>
    <td class="reffield">
       <?php 
             for ($i=0, $length=count($value['example']); $i<$length; $i++) {
                 $e = $value['example'][$i];
       ?>
               <table border="0" cellspacing="0" cellpadding="0">
                 <tr>
       <?php     if (isset($e['image'])) { ?>
                   <td valign="top">
       <?php         if (strstr($e['image'], '.jar') !== false) { ?>
                       <applet code="<?php echo substr($e['image'], 0, strlen($e['image']) - 4) ?>"
                               archive="API/media/<?php echo $e['image'] ?>"
                               width="100" height="100">
                       </applet>
       <?php         } else { ?>
                       <img src="API/media/<?php echo $e['image'] ?>">
       <?php         } ?>
                   </td>
                   <td width="20">&nbsp;</td>
       <?php     } ?>
                 <td valign="top"><pre><?php echo $e['code'] ?></pre></td>
                 </tr>
               </table>
       <?php     if ($i < ($length - 1)) { ?>
               <hr size="1">
       <?php     } ?>
               <br>
       <?php } ?>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['description'])) { ?>
  <tr>
    <td class="reffieldheader">Description</td>
    <td class="reffield">
      <?php echo $value['description'] ?>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['syntax'])) { ?>
  <tr>
    <td class="reffieldheader">Syntax</td>
    <td class="reffield">
      <pre><?php echo $value['syntax'] ?></pre>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['parameter'])) { ?>
  <tr>
    <td class="reffieldheader">Parameters</td>
    <td class="reffield">
      <table border="0" cellspacing="0" cellpadding="0">
       <?php foreach ($value['parameter'] as $p) { ?>
                 <tr>
                   <td valign="top" width="70"><?php echo $p['label'] ?></td>
                   <td width="20">&nbsp;</td>
                   <td valign="top"><?php echo $p['description'] ?><br><br></td>
                 </tr>
       <?php } ?>
      </table>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['returns'])) { ?>
  <tr>
    <td class="reffieldheader">Returns</td>
    <td class="reffield"><?php echo $value['returns'] ?></td>
  </tr>
<?php } ?>
<?php if (isset($value['field'])) { ?>
  <tr>
    <td class="reffieldheader">Fields</td>
    <td class="reffield">
      <table border="0" cellspacing="0" cellpadding="0">
       <?php foreach ($value['field'] as $f) { ?>
                 <tr>
                   <td valign="top" width="70">
                     <a href="reference.php?name=<?php echo $_GET['name'] ?>_<?php echo rtrim($f['fname'], "[]() ") ?>"><?php echo $f['fname'] ?></a>
                   </td>
                   <td valign="top" width="20">&nbsp;</td>
                   <td><?php echo $f['fdescription'] ?><br><br></td>
                 </tr>
       <?php } ?>
      </table>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['method'])) { ?>
  <tr>
    <td class="reffieldheader">Methods</td>
    <td class="reffield">
      <table border="0" cellspacing="0" cellpadding="0">
       <?php foreach ($value['method'] as $m) { ?>
                 <tr>
                   <td valign="top" width="70">
                     <a href="reference.php?name=<?php echo $_GET['name'] ?>_<?php echo rtrim($m['mname'], "() ") ?>"><?php echo $m['mname'] ?></a>
                   </td>
                   <td valign="top" width="20">&nbsp;</td>
                   <td><?php echo $m['mdescription'] ?><br><br></td>
                 </tr>
       <?php } ?>
      </table>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['constructor'])) { ?>
  <tr>
    <td class="reffieldheader">Constructors</td>
    <td class="reffield">
      <pre><?php echo $value['constructor'] ?></pre>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['cparameter'])) { ?>
  <tr>
    <td class="reffieldheader">Parameters</td>
    <td class="reffield">
      <table border="0" cellspacing="0" cellpadding="0">
       <?php foreach ($value['cparameter'] as $p) { ?>
                 <tr>
                   <td valign="top" width="70"><?php echo $p['clabel'] ?></td>
                   <td width="20">&nbsp;</td>
                   <td vakugb="top">
                     <?php echo $p['cdescription'] ?><br><br>
                   </td>
                 </tr>
       <?php } ?>
      </table>
    </td>
  </tr>
<?php } ?>
<?php if (isset($value['related'])) { ?>
  <tr>
    <td class="reffieldheader">Related</td>
    <td class="reffield">
       <?php foreach ($value['related'] as $f) { ?>
         <a href="reference.php?name=<?php echo rtrim($f, "() ") ?>"><?php echo $f ?></a><br>
       <?php } ?>
    </td>
  </tr>
<?php } ?>
</table>

